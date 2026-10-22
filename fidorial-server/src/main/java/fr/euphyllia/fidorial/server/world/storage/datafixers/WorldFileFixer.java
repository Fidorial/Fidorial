package fr.euphyllia.fidorial.server.world.storage.datafixers;

import fr.euphyllia.fidorial.server.world.storage.LevelData;
import fr.euphyllia.fidorial.server.world.storage.WorldPathsImpl;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public final class WorldFileFixer {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldFileFixer.class);

    private static final Path VANILLA_WORLD_CLOCKS_PATH = Path.of("minecraft", "world_clocks.dat");

    private WorldFileFixer() {
        throw new UnsupportedOperationException("WorldFileFixer cannot be instantiated.");
    }

    public static void run(final WorldPathsImpl paths) throws IOException {
        moveWorldClocks(paths.dataDir());
        movePlayerFiles(paths.worldRoot());
    }

    private static void moveWorldClocks(final Path dataDir) throws IOException {
        final Path legacy = dataDir.resolve(VANILLA_WORLD_CLOCKS_PATH);
        final Path target = dataDir.resolve(LevelData.WORLD_CLOCKS_PATH);
        if (!Files.isRegularFile(legacy) || Files.exists(target)) {
            return;
        }
        final CompoundBinaryTag root = BinaryTagIO.reader().readNamed(legacy, BinaryTagIO.Compression.GZIP).getValue();
        if (!root.contains("WorldClocks") && !root.getCompound("data").contains("WorldClocks")) {
            return; // vanilla's own file
        }
        Files.createDirectories(target.getParent());
        Files.move(legacy, target);
        LOGGER.info("Moved Fidorial world clocks to {}", target);
    }

    /**
     * {@code player/} to {@code players/}, and {@code players/data/<uuid>} to {@code <uuid>.dat}.
     */
    private static void movePlayerFiles(final Path worldRoot) throws IOException {
        final Path legacy = worldRoot.resolve("player");
        if (Files.isDirectory(legacy)) {
            merge(legacy, worldRoot.resolve("players"));
            LOGGER.info("Moved {} into {}", legacy, worldRoot.resolve("players"));
        }

        final Path data = worldRoot.resolve("players").resolve("data");
        if (!Files.isDirectory(data)) {
            return;
        }
        final List<Path> unsuffixed;
        try (final Stream<Path> files = Files.list(data)) {
            unsuffixed = files.filter(Files::isRegularFile).filter(file -> isUuid(file.getFileName().toString())).toList();
        }
        for (final Path file : unsuffixed) {
            final Path target = file.resolveSibling(file.getFileName() + ".dat");
            if (Files.exists(target)) {
                LOGGER.warn("Not converting {}: {} already exists", file, target);
                continue;
            }
            Files.move(file, target);
        }
    }

    private static void merge(final Path source, final Path target) throws IOException {
        if (!Files.exists(target)) {
            final Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.move(source, target);
            return;
        }
        if (!Files.isDirectory(source) || !Files.isDirectory(target)) {
            LOGGER.warn("Not moving {}: {} already exists", source, target);
            return;
        }
        final List<Path> children;
        try (final Stream<Path> stream = Files.list(source)) {
            children = stream.toList();
        }
        for (final Path child : children) {
            merge(child, target.resolve(child.getFileName().toString()));
        }
        try (final Stream<Path> rest = Files.list(source)) {
            if (rest.findAny().isEmpty()) {
                Files.delete(source);
            }
        }
    }

    private static boolean isUuid(final String name) {
        if (name.length() != 36) {
            return false;
        }
        try {
            UUID.fromString(name);
            return true;
        } catch (final IllegalArgumentException notUuid) {
            return false;
        }
    }
}
