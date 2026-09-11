package fr.euphyllia.fidorial.server.world.structure.pool;

public enum Projection {
    RIGID,
    TERRAIN_MATCHING;

    public static Projection byName(final String name) {
        return "terrain_matching".equals(name) ? TERRAIN_MATCHING : RIGID;
    }
}
