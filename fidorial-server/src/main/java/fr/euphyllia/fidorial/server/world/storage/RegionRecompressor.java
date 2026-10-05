package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.euphyllia.fidorial.server.world.anvil.RegionConstants;
import fr.euphyllia.fidorial.server.world.anvil.RegionFile;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Rewrites the saved chunks and entities of a dimension when {@code storage.region-compression} changes.
 * <p>
 * The last compression fully applied to a dimension is recorded in {@code data/fidorial/region-compression}, next
 * to its {@code fidorial-world.yaml}; a dimension without this file is in {@link RegionCompression#DEFAULT}, the only
 * compression Fidorial wrote before the setting existed. When the world loads with another compression configured,
 * every chunk stored with a different one is rewritten, then the file is updated. This happens once per change.
 * <p>
 * Chunks already in the target compression are skipped (only their header is read), so an interrupted
 * conversion resumes where it stopped. A chunk that cannot be read is left as it is, and the conversion is tried
 * again the next time the world loads.
 * <p>
 * Regardless of this, {@link ChunkStorage} and {@link EntityRegionStorage} rewrite any chunk they load that is stored
 * with another compression than the configured one.
 */
public final class RegionRecompressor {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(RegionRecompressor.class);
    private static final String COMPRESSION_FILE = "region-compression";
    private static final Pattern REGION_FILE = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
    private static final long PROGRESS_INTERVAL = TimeUnit.SECONDS.toNanos(10);

    /**
     * Gives the {@link RegionFile} already open for a region, or {@code null} if it is not cached.
     */
    @FunctionalInterface
    interface OpenRegions {
        @Nullable RegionFile cached(int regionX, int regionZ);
    }

    record Result(int files, int converted, int failed) {

        static final Result EMPTY = new Result(0, 0, 0);

        Result plus(final Result other) {
            return new Result(files + other.files, converted + other.converted, failed + other.failed);
        }
    }

    private RegionRecompressor() {
    }

    /**
     * Rewrites the chunks of {@code dim} in {@code target} if it changed since the last time. Must be called before
     * the dimension is loaded: no chunk of it may be read or written during the call.
     *
     * @param convertExisting {@code false} to only convert chunks as they load
     * @throws IllegalStateException if {@code target} cannot be used on this machine
     */
    public static void convertIfChanged(final WorldPaths paths, final Dimension dim, final ChunkStorage chunks,
                                        final EntityRegionStorage entities, final RegionCompression target, final boolean convertExisting) {
        target.checkAvailable();
        if (!convertExisting) {
            return;
        }
        final Path compressionFile = paths.configFile(dim).resolveSibling(COMPRESSION_FILE);
        final RegionCompression current = readCompression(compressionFile);
        if (current == target) {
            return;
        }
        final String world = dim.id().asString();
        try {
            if (Files.isDirectory(paths.regionDir(dim)) || Files.isDirectory(paths.entitiesDir(dim))) {
                LOGGER.info("Converting the saved chunks of {} to {} compression (was {}). This only happens once, but may take a while on a large world",
                        world, target.configName(), current == null ? "unknown" : current.configName());
                final long start = System.nanoTime();
                final Result result = chunks.convertAll(dim, target).plus(entities.convertAll(dim, target));
                LOGGER.info("Converted {}: {} chunk(s) rewritten in {} region file(s) in {} s", world, result.converted(),
                        result.files(), TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
                if (result.failed() > 0) {
                    LOGGER.warn("{} chunk(s) of {} could not be converted and were left as they were; "
                            + "the conversion will be tried again the next time the world loads", result.failed(), world);
                    return;
                }
            }
            writeCompression(compressionFile, target);
        } catch (final IOException e) {
            LOGGER.error("Could not convert the saved chunks of {} to {} compression; they will be converted as they load",
                    world, target.configName(), e);
        }
    }

    /**
     * Converts every region file of {@code directory}. No other file of this directory may be opened during the call;
     * the files already open ({@code open}) are used while holding their lock.
     */
    static Result convertDirectory(final Path directory, final RegionCompression target, final OpenRegions open) throws IOException {
        if (!Files.isDirectory(directory)) {
            return Result.EMPTY;
        }
        final List<Path> files = new ArrayList<>();
        try (final Stream<Path> list = Files.list(directory)) {
            list.filter(file -> REGION_FILE.matcher(file.getFileName().toString()).matches()).sorted().forEach(files::add);
        }

        Result total = Result.EMPTY;
        long lastProgress = System.nanoTime();
        for (int n = 0; n < files.size(); n++) {
            final Path file = files.get(n);
            final Matcher matcher = REGION_FILE.matcher(file.getFileName().toString());
            if (!matcher.matches() || Files.size(file) == 0) {
                continue;
            }
            final RegionFile cached = open.cached(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            if (cached != null) {
                synchronized (cached) {
                    total = total.plus(convertFile(cached, file, target));
                }
            } else {
                try (final RegionFile region = new RegionFile(file)) {
                    total = total.plus(convertFile(region, file, target));
                }
            }
            if (System.nanoTime() - lastProgress > PROGRESS_INTERVAL) {
                lastProgress = System.nanoTime();
                LOGGER.info("Converting {} to {}: {}/{} files, {} chunk(s) rewritten",
                        directory, target.configName(), n + 1, files.size(), total.converted());
            }
        }
        return total;
    }

    private static Result convertFile(final RegionFile region, final Path file, final RegionCompression target) {
        int converted = 0;
        int failed = 0;
        for (int index = 0; index < RegionConstants.CHUNKS_PER_REGION; index++) {
            final int localX = index & (RegionConstants.REGION_SIZE - 1);
            final int localZ = index / RegionConstants.REGION_SIZE;
            if (!region.hasChunk(localX, localZ)) {
                continue;
            }
            try {
                if (region.recompress(localX, localZ, target)) {
                    converted++;
                }
            } catch (final IOException e) {
                failed++;
                LOGGER.warn("Could not convert chunk {},{} of {} to {}: {}", localX, localZ, file, target.configName(), e.getMessage());
            }
        }
        return new Result(1, converted, failed);
    }

    /**
     * {@return the compression recorded in {@code file}, {@link RegionCompression#DEFAULT} if there is no file, or
     * {@code null} if it cannot be read, which forces a full conversion}
     */
    private static @Nullable RegionCompression readCompression(final Path file) {
        if (!Files.isRegularFile(file)) {
            return RegionCompression.DEFAULT;
        }
        try {
            final RegionCompression recorded = RegionCompression.byName(Files.readString(file, StandardCharsets.UTF_8));
            if (recorded != null) {
                return recorded;
            }
            LOGGER.warn("Ignoring {}: unknown compression", file);
        } catch (final IOException e) {
            LOGGER.warn("Could not read {}: {}", file, e.getMessage());
        }
        return null;
    }

    private static void writeCompression(final Path file, final RegionCompression compression) throws IOException {
        Files.createDirectories(file.getParent());
        final Path temp = file.resolveSibling(COMPRESSION_FILE + ".tmp");
        Files.writeString(temp, compression.configName() + "\n", StandardCharsets.UTF_8);
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
