package fr.euphyllia.fidorial.server.entity.player;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.fluid.FluidBlockCodec;
import fr.fidorial.annotation.SinceMinecraft;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.fluid.FluidState;
import fr.fidorial.world.fluid.FluidType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@SinceMinecraft("26.4")
final class FallDistanceModifiers {

    private static final Set<Key> ALWAYS_PORTALS = Set.of(BlockTypeKeys.END_PORTAL.key(), BlockTypeKeys.END_GATEWAY.key());
    private static final Key NETHER_PORTAL = BlockTypeKeys.NETHER_PORTAL.key();
    /**
     * The blocks whose fluid is always a water source, without a {@code waterlogged} property.
     */
    private static final Set<Key> ALWAYS_WATERLOGGED = Set.of(
            BlockTypeKeys.BUBBLE_COLUMN.key(),
            BlockTypeKeys.KELP.key(),
            BlockTypeKeys.KELP_PLANT.key(),
            BlockTypeKeys.SEAGRASS.key(),
            BlockTypeKeys.TALL_SEAGRASS.key());

    private static final float WATER_MULTIPLIER = 0.0f;
    private static final float LAVA_MULTIPLIER = 0.5f;

    private static final double HALF_WIDTH = 0.3;
    private static final double HEIGHT = 1.8;
    /**
     * Beyond this many cells (a teleport-sized movement), only the destination is looked at.
     */
    private static final int MAX_SWEPT_CELLS = 4096;
    private static final double SEGMENT_STEP = 0.25;

    private FallDistanceModifiers() {
    }

    /**
     * {@return the fall distance after a movement}
     *
     * @param world        the world the player moves in
     * @param from         where the movement starts
     * @param to           where it ends
     * @param fallDistance the fall distance before it
     */
    static double afterMovement(final ServerWorld world, final Location from, final Location to, final double fallDistance) {
        final double dy = to.y() - from.y();
        double distance = fallDistance;
        boolean touchedWater = false;

        if (dy < 0.0) {
            final Sweep sweep = sweep(world, from, to);
            if (sweep.resets()) {
                distance = 0.0;
            }
            touchedWater = sweep.touchedWater();
            distance -= dy;
        }
        if (distance <= 0.0) {
            return 0.0;
        }

        final float multiplier = touchedWater ? WATER_MULTIPLIER : fluidMultiplierAt(world, to);
        return distance * multiplier;
    }

    private static Sweep sweep(final ServerWorld world, final Location from, final Location to) {
        final int minX = floor(Math.min(from.x(), to.x()) - HALF_WIDTH);
        final int maxX = floor(Math.max(from.x(), to.x()) + HALF_WIDTH);
        final int minY = floor(Math.min(from.y(), to.y()));
        final int maxY = floor(Math.max(from.y(), to.y()) + HEIGHT);
        final int minZ = floor(Math.min(from.z(), to.z()) - HALF_WIDTH);
        final int maxZ = floor(Math.max(from.z(), to.z()) + HALF_WIDTH);
        final long cells = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (cells > MAX_SWEPT_CELLS) {
            return new Sweep(false, false);
        }

        final boolean netherPortalResets =
                world.gameRuleValues().getInt(GameRuleKeys.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY) == 0;
        final double bottom = Math.min(from.y(), to.y());
        boolean resets = false;
        boolean touchedWater = false;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final BlockState state = BlockView.blockAt(world, x, y, z);
                    if (state == null || state.isAir()) {
                        continue;
                    }
                    final Key name = state.name();
                    if (ALWAYS_PORTALS.contains(name) || netherPortalResets && name.equals(NETHER_PORTAL)) {
                        resets = true;
                    } else if (BlockView.isTagged(BlockTypeTagKeys.FALL_DAMAGE_RESETTING.key(), state)
                            && centerCrosses(from, to, x, y, z)) {
                        resets = true;
                    }
                    if (!touchedWater && fluidOf(state) == FluidType.WATER
                            && y + fluidHeight(world, state, FluidType.WATER, x, y, z) > bottom) {
                        touchedWater = true;
                    }
                }
            }
        }
        return new Sweep(resets, touchedWater);
    }

    /**
     * {@return the lowest multiplier of the fluids the player stands in at {@code at}}
     */
    private static float fluidMultiplierAt(final ServerWorld world, final Location at) {
        final int minX = floor(at.x() - HALF_WIDTH);
        final int maxX = floor(at.x() + HALF_WIDTH);
        final int minY = floor(at.y());
        final int maxY = floor(at.y() + HEIGHT);
        final int minZ = floor(at.z() - HALF_WIDTH);
        final int maxZ = floor(at.z() + HALF_WIDTH);
        float multiplier = 1.0f;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final BlockState state = BlockView.blockAt(world, x, y, z);
                    final FluidType fluid = state == null ? null : fluidOf(state);
                    if (fluid == null || y + fluidHeight(world, state, fluid, x, y, z) <= at.y()) {
                        continue;
                    }
                    if (fluid == FluidType.WATER) {
                        return WATER_MULTIPLIER;
                    }
                    multiplier = Math.min(multiplier, LAVA_MULTIPLIER);
                }
            }
        }
        return multiplier;
    }

    private static @Nullable FluidType fluidOf(final BlockState state) {
        final FluidType type = FluidType.byBlockKey(state.name());
        if (type != null) {
            return type;
        }
        if ("true".equals(state.properties().get("waterlogged")) || ALWAYS_WATERLOGGED.contains(state.name())) {
            return FluidType.WATER;
        }
        return null;
    }

    /**
     * {@return the height of the fluid in a cell: a full block under the same fluid, its amount out
     * of 9 otherwise}
     */
    private static double fluidHeight(final ServerWorld world, final BlockState state, final FluidType type,
                                      final int x, final int y, final int z) {
        final BlockState above = BlockView.blockAt(world, x, y + 1, z);
        if (above != null && fluidOf(above) == type) {
            return 1.0;
        }
        final FluidState fluid = FluidBlockCodec.fromBlock(state);
        final int amount = fluid.isEmpty() ? 8 : 8 - fluid.effectiveLevel();
        return amount / 9.0;
    }

    /**
     * {@return {@code true} if the segment followed by the player's feet goes through a cell}
     */
    private static boolean centerCrosses(final Location from, final Location to, final int x, final int y, final int z) {
        final double dx = to.x() - from.x();
        final double dy = to.y() - from.y();
        final double dz = to.z() - from.z();
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        final int steps = Math.max(1, (int) Math.ceil(length / SEGMENT_STEP));
        for (int i = 0; i <= steps; i++) {
            final double t = (double) i / steps;
            if (floor(from.x() + dx * t) == x && floor(from.y() + dy * t) == y && floor(from.z() + dz * t) == z) {
                return true;
            }
        }
        return false;
    }

    private static int floor(final double value) {
        return (int) Math.floor(value);
    }

    private record Sweep(boolean resets, boolean touchedWater) {
    }
}
