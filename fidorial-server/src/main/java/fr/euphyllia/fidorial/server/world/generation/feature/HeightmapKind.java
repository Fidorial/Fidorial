package fr.euphyllia.fidorial.server.world.generation.feature;

import java.util.Locale;

/**
 * The column heights a decoration can ask for: the first free block above the highest block that counts for the
 * kind.
 *
 * <p>The two terrain kinds are taken once the terrain is done and never move afterwards; the others follow every
 * block the decorations set.</p>
 */
public enum HeightmapKind {

    /**
     * Any block but air, as the terrain left it.
     */
    WORLD_SURFACE_WG(false),
    /**
     * Any block but air.
     */
    WORLD_SURFACE(true),
    /**
     * The blocks that stop a moving entity, as the terrain left them: water does not count.
     */
    OCEAN_FLOOR_WG(false),
    /**
     * The blocks that stop a moving entity: water does not count.
     */
    OCEAN_FLOOR(true),
    /**
     * The blocks that stop a moving entity, and every fluid.
     */
    MOTION_BLOCKING(true),
    /**
     * Like {@link #MOTION_BLOCKING}, leaves left out.
     */
    MOTION_BLOCKING_NO_LEAVES(true);

    private static final HeightmapKind[] VALUES = values();

    private final boolean live;

    HeightmapKind(final boolean live) {
        this.live = live;
    }

    /**
     * {@return {@code true} if the height follows the blocks set by the decorations}
     */
    public boolean live() {
        return live;
    }

    /**
     * {@return whether a block counts for this kind}
     */
    public boolean counts(final StateFlags states, final char id) {
        return switch (this) {
            case WORLD_SURFACE_WG, WORLD_SURFACE -> !states.isAir(id);
            case OCEAN_FLOOR_WG, OCEAN_FLOOR -> states.has(id, StateFlags.MOTION);
            case MOTION_BLOCKING -> states.has(id, StateFlags.MOTION) || states.hasFluid(id);
            case MOTION_BLOCKING_NO_LEAVES -> states.has(id, StateFlags.MOTION_NO_LEAVES) || states.hasFluid(id);
        };
    }

    /**
     * Reads a kind by its data name, such as {@code WORLD_SURFACE_WG}.
     */
    public static HeightmapKind parse(final String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    static HeightmapKind byOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    static int count() {
        return VALUES.length;
    }
}
