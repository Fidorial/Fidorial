package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.fidorial.world.WorldPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class WorldPathsImpl implements WorldPaths.RootWorldPaths {

    private final Path worldRoot;
    private final Layout writeLayout;

    public WorldPathsImpl(final Path worldRoot) {
        this(worldRoot, Layout.MODERN);
    }

    public WorldPathsImpl(final Path worldRoot, final Layout writeLayout) {
        this.worldRoot = worldRoot;
        this.writeLayout = writeLayout;
    }

    public Path worldRoot() {
        return worldRoot;
    }

    public Path levelDat() {
        return worldRoot.resolve("level.dat");
    }

    private Path modernDimensionRoot(final Dimension dim) {
        return worldRoot.resolve("dimensions").resolve(dim.id().namespace()).resolve(dim.id().value());
    }

    private Path legacyDimensionRoot(final Dimension dim) {
        return dim.legacyFolder() == null ? worldRoot : worldRoot.resolve(dim.legacyFolder());
    }

    public Path dimensionDir(final Dimension dim) {
        return writeLayout == Layout.MODERN ? modernDimensionRoot(dim) : legacyDimensionRoot(dim);
    }

    public Path regionDir(final Dimension dim) {
        return dimensionDir(dim).resolve("region");
    }

    public Path entitiesDir(final Dimension dim) {
        return dimensionDir(dim).resolve("entities");
    }

    // Maybe we should store data per-dimension like Paper for multi-world
    @NeedsToBeRevisited("Might not allow for proper multi-dimension support")
    public Path dataDir() {
        return worldRoot.resolve("data");
    }

    public Path dimensionDataDir(final Dimension dim) {
        return dimensionDir(dim).resolve("data");
    }

    public Path poiDir(final Dimension dim) {
        return dimensionDir(dim).resolve("poi");
    }

    public WorldPaths forDimension(final Dimension dim) {
        return new DimensionPaths(dimensionDir(dim), dimensionDataDir(dim), this);
    }

    @Override
    public Path directory() {
        return worldRoot;
    }

    @Override
    public Path dataDirectory() {
        return dataDir();
    }

    public Path regionDirForRead(final Dimension dim) {
        final Path modern = modernDimensionRoot(dim).resolve("region");
        if (Files.isDirectory(modern)) return modern;
        final Path legacy = legacyDimensionRoot(dim).resolve("region");
        if (Files.isDirectory(legacy)) return legacy;
        return modern;
    }

    public Path configFile(final Dimension dim) {
        final Path base = dimensionDataDir(dim);
        return base.resolve("fidorial/fidorial-world.yaml");
    }

    public enum Layout {
        MODERN,
        LEGACY
    }

    private record DimensionPaths(
            Path directory,
            Path dataDirectory,
            WorldPaths.RootWorldPaths rootPaths
    ) implements WorldPaths {
    }

}
