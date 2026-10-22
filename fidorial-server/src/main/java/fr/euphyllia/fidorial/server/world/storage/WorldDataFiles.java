package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public final class WorldDataFiles {

    private static final String DATA = "data";
    private static final String DATA_VERSION = "DataVersion";

    private WorldDataFiles() {
        throw new UnsupportedOperationException("WorldDataFiles cannot be instantiated.");
    }

    public static Optional<CompoundBinaryTag> read(final Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        final CompoundBinaryTag root = BinaryTagIO.reader().readNamed(path, BinaryTagIO.Compression.GZIP).getValue();
        return Optional.of(DataFixersRegistry.update(DataFixerType.WORLD_DATA, root, false).getCompound(DATA));
    }

    public static void write(final Path path, final CompoundBinaryTag payload) throws IOException {
        Files.createDirectories(path.getParent());
        final CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put(DATA, payload)
                .putInt(DATA_VERSION, VersionConstants.DATA_VERSION)
                .build();
        BinaryTagIO.writer().writeNamed(Map.entry("", root), path, BinaryTagIO.Compression.GZIP);
    }
}
