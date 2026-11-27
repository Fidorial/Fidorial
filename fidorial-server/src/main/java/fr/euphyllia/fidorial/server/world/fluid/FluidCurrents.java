package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.SupportType;

import java.util.Set;

/**
 * How the currents of water and lava push the entities in them.
 *
 * <p>Each block of fluid an entity touches flows towards the lower fluid around it, and down along
 * a wall when it falls. The flows of a fluid add up, weakened where the entity barely dips into it,
 * and the entity drifts along their direction: by 0.014 of a block per tick in water, 0.007 in lava
 * where it is fast and 0.0023 elsewhere.</p>
 */
public final class FluidCurrents {

    /**
     * No push at all.
     */
    public static final Impulse NONE = new Impulse(0.0, 0.0, 0.0);

    private static final double WATER_FLOW_SCALE = 0.014;
    private static final double FAST_LAVA_FLOW_SCALE = 0.007;
    private static final double SLOW_LAVA_FLOW_SCALE = 0.0023333333333333335;

    /**
     * How deep an entity must be in a fluid to feel its whole current.
     */
    private static final double SHALLOW = 0.4;

    /**
     * How far inside its box an entity starts touching fluid.
     */
    private static final double MARGIN = 0.001;

    /**
     * The least push an entity standing still in a current gets, so that it starts drifting.
     */
    private static final double LEAST_PUSH = 0.0045000000000000005;

    /**
     * How fast an entity must already go for {@link #LEAST_PUSH} not to apply, squared.
     */
    private static final double STILL_SPEED_SQUARED = 9.0E-6;

    /**
     * Below this length a vector has no direction.
     */
    private static final double NO_LENGTH = 1.0E-5F;

    /**
     * How much lower than a block of fluid the fluid one block down the slope stands.
     */
    private static final float DOWN_THE_SLOPE = 0.8888889f;

    /**
     * How hard falling fluid pulls down along a wall, against a horizontal flow of one.
     */
    private static final double FALLING_PULL = -6.0;

    private static final BlockFace[] HORIZONTAL = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    /**
     * The ice blocks whose faces never count as walls for falling fluid.
     */
    private static final Set<TypedKey<BlockType>> ICE = Set.of(BlockTypeKeys.ICE, BlockTypeKeys.FROSTED_ICE, BlockTypeKeys.BRITTLE_ICE);

    /**
     * A push, in blocks per tick.
     *
     * @param x along the x axis
     * @param y along the y axis
     * @param z along the z axis
     */
    public record Impulse(double x, double y, double z) {

        /**
         * {@return {@code true} if this pushes nowhere}
         */
        public boolean isNone() {
            return x == 0.0 && y == 0.0 && z == 0.0;
        }
    }

    /**
     * {@return how the currents push an entity}, none when it touches no fluid or stands next to a
     * chunk that is not loaded
     *
     * @param world     the world of the entity
     * @param minX      the lowest x of the box of the entity
     * @param minY      the lowest y of the box of the entity, its feet
     * @param minZ      the lowest z of the box of the entity
     * @param maxX      the highest x of the box of the entity
     * @param maxY      the highest y of the box of the entity
     * @param maxZ      the highest z of the box of the entity
     * @param velocityX how fast the entity already goes along the x axis
     * @param velocityZ how fast the entity already goes along the z axis
     */
    public Impulse push(final ServerWorld world, final double minX, final double minY, final double minZ,
                        final double maxX, final double maxY, final double maxZ,
                        final double velocityX, final double velocityZ) {
        final double boxMinY = minY + MARGIN;
        final int x0 = floor(minX + MARGIN);
        final int y0 = floor(boxMinY);
        final int z0 = floor(minZ + MARGIN);
        final int x1 = ceil(maxX - MARGIN) - 1;
        final int y1 = ceil(maxY - MARGIN) - 1;
        final int z1 = ceil(maxZ - MARGIN) - 1;
        if (!loaded(world, x0 - 1, z0 - 1, x1 + 1, z1 + 1)) {
            return NONE;
        }
        final Current water = new Current();
        final Current lava = new Current();
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    final FluidStateImpl fluid = fluidAt(world, x, y, z);
                    if (fluid.isEmpty()) {
                        continue;
                    }
                    final double top = y + height(world, x, y, z, fluid);
                    if (top < boxMinY) {
                        continue;
                    }
                    final Current current = fluid.type() == FluidType.WATER ? water : lava;
                    current.height = Math.max(top - minY, current.height);
                    double[] flow = flow(world, x, y, z, fluid);
                    if (current.height < SHALLOW) {
                        flow = scale(flow, current.height);
                    }
                    current.add(flow);
                }
            }
        }
        final double[] waterPush = water.impulse(WATER_FLOW_SCALE);
        final double[] lavaPush = lava.impulse(FluidEngine.isFastLava(world) ? FAST_LAVA_FLOW_SCALE : SLOW_LAVA_FLOW_SCALE);
        double[] impulse = {waterPush[0] + lavaPush[0], waterPush[1] + lavaPush[1], waterPush[2] + lavaPush[2]};
        final double length = length(impulse);
        final double movingX = velocityX + impulse[0];
        final double movingZ = velocityZ + impulse[2];
        if (movingX * movingX + movingZ * movingZ < STILL_SPEED_SQUARED && length > NO_LENGTH && length < LEAST_PUSH) {
            impulse = scale(normalize(impulse), LEAST_PUSH);
        }
        return impulse[0] == 0.0 && impulse[1] == 0.0 && impulse[2] == 0.0 ? NONE : new Impulse(impulse[0], impulse[1], impulse[2]);
    }

    /**
     * {@return the direction a block of fluid flows in}, of length one or none: towards the lower
     * fluid beside it, or the open blocks it would flow down into, and down along a wall when it
     * falls
     */
    double[] flow(final ServerWorld world, final int x, final int y, final int z, final FluidStateImpl fluid) {
        double flowX = 0.0;
        double flowZ = 0.0;
        for (final BlockFace direction : HORIZONTAL) {
            final int nx = x + direction.dx();
            final int nz = z + direction.dz();
            final FluidStateImpl neighbour = fluidAt(world, nx, y, nz);
            if (!affectsFlow(fluid, neighbour)) {
                continue;
            }
            float neighbourHeight = neighbour.ownHeight();
            float distance = 0.0f;
            if (neighbourHeight == 0.0f) {
                final BlockState neighbourBlock = BlockView.blockAt(world, nx, y, nz);
                if (neighbourBlock != null && !neighbourBlock.isTagged(BlockTypeTagKeys.BLOCKS_FLUID_FLOW)) {
                    final FluidStateImpl below = fluidAt(world, nx, y - 1, nz);
                    if (affectsFlow(fluid, below)) {
                        neighbourHeight = below.ownHeight();
                        if (neighbourHeight > 0.0f) {
                            distance = fluid.ownHeight() - (neighbourHeight - DOWN_THE_SLOPE);
                        }
                    }
                }
            } else if (neighbourHeight > 0.0f) {
                distance = fluid.ownHeight() - neighbourHeight;
            }
            if (distance != 0.0f) {
                flowX += direction.dx() * distance;
                flowZ += direction.dz() * distance;
            }
        }
        double[] flow = {flowX, 0.0, flowZ};
        if (fluid.isFalling()) {
            for (final BlockFace direction : HORIZONTAL) {
                final int nx = x + direction.dx();
                final int nz = z + direction.dz();
                if (isWall(world, nx, y, nz, direction, fluid) || isWall(world, nx, y + 1, nz, direction, fluid)) {
                    flow = normalize(flow);
                    flow[1] += FALLING_PULL;
                    break;
                }
            }
        }
        return normalize(flow);
    }

    /**
     * {@return {@code true} if a block stops falling fluid beside it}: one whose face away from the
     * fluid is whole, ice and the same fluid aside
     */
    private boolean isWall(final ServerWorld world, final int x, final int y, final int z, final BlockFace direction,
                           final FluidStateImpl fluid) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        if (state == null || FluidBlockCodec.fromBlock(state).type() == fluid.type() || ICE.contains(state.name())) {
            return false;
        }
        return BlockSupport.isFaceSturdy(state.data(), direction, SupportType.FULL);
    }

    private static boolean affectsFlow(final FluidStateImpl fluid, final FluidStateImpl neighbour) {
        return neighbour.isEmpty() || neighbour.type() == fluid.type();
    }

    private static FluidStateImpl fluidAt(final ServerWorld world, final int x, final int y, final int z) {
        return FluidBlockCodec.fromBlock(BlockView.blockAt(world, x, y, z));
    }

    /**
     * {@return how high a fluid stands in its block}: whole under the same fluid
     */
    private static float height(final ServerWorld world, final int x, final int y, final int z, final FluidStateImpl fluid) {
        return fluidAt(world, x, y + 1, z).type() == fluid.type() ? 1.0f : fluid.ownHeight();
    }

    private static boolean loaded(final ServerWorld world, final int x0, final int z0, final int x1, final int z1) {
        for (int chunkX = x0 >> 4; chunkX <= x1 >> 4; chunkX++) {
            for (int chunkZ = z0 >> 4; chunkZ <= z1 >> 4; chunkZ++) {
                if (world.loadedColumn(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double[] normalize(final double[] vector) {
        final double length = length(vector);
        return length < NO_LENGTH ? new double[3] : new double[] {vector[0] / length, vector[1] / length, vector[2] / length};
    }

    private static double[] scale(final double[] vector, final double factor) {
        return new double[] {vector[0] * factor, vector[1] * factor, vector[2] * factor};
    }

    private static double length(final double[] vector) {
        return Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
    }

    private static int floor(final double value) {
        return (int) Math.floor(value);
    }

    private static int ceil(final double value) {
        return (int) Math.ceil(value);
    }

    /**
     * The flows of one fluid an entity touches, added up.
     */
    private static final class Current {

        private double height;
        private final double[] flow = new double[3];
        private int count;

        void add(final double[] other) {
            flow[0] += other[0];
            flow[1] += other[1];
            flow[2] += other[2];
            count++;
        }

        double[] impulse(final double scale) {
            final double lengthSquared = flow[0] * flow[0] + flow[1] * flow[1] + flow[2] * flow[2];
            if (count == 0 || lengthSquared < NO_LENGTH) {
                return new double[3];
            }
            return scale(normalize(flow), scale);
        }
    }
}
