package fr.euphyllia.fidorial.server.world.structure.worldgen;

import java.util.Locale;

public enum Heightmap {
    WORLD_SURFACE_WG(false),
    WORLD_SURFACE(false),
    MOTION_BLOCKING(false),
    MOTION_BLOCKING_NO_LEAVES(false),
    OCEAN_FLOOR_WG(true),
    OCEAN_FLOOR(true);

    private final boolean floor;

    Heightmap(final boolean floor) {
        this.floor = floor;
    }

    public boolean ignoresFluids() {
        return floor;
    }

    public static Heightmap byName(final String name, final Heightmap fallback) {
        try {
            return valueOf(name.strip().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException unknown) {
            return fallback;
        }
    }
}
