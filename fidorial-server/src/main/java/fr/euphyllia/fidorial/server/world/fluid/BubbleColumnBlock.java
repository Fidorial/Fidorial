package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelParticlesPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ParticleType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.ParticleTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A bubble column: water sources a block of the {@code enables_bubble_column_push_up} tag below
 * makes rise, or one of the {@code enables_bubble_column_drag_down} tag makes sink.
 *
 * <p>The column reaches up through every water source above it. It is a water source itself, and
 * turns back into water once nothing below holds it up.</p>
 *
 * <p>Each block of column an entity goes through pushes it: up by 0.06 of a block per tick, at
 * most up to 0.7, or down by 0.03, at most down to 0.3. The top block of a column, with neither a
 * block to bump into nor fluid above it, pushes harder, up by 0.1 up to 1.8, or down by 0.03 down
 * to 0.9, throwing up splashes and bubbles.</p>
 */
public final class BubbleColumnBlock implements RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.BUBBLE_COLUMN;
    public static final BubbleColumnBlock INSTANCE = new BubbleColumnBlock();

    private static final String DRAG = "drag";
    /**
     * How long a column waits before following a change below or above it, in game ticks.
     */
    private static final int CHECK_DELAY = 5;
    private static final int WATER_TICK_DELAY = 5;

    private static final double INSIDE_RISE = 0.06;
    private static final double INSIDE_MAX_RISE = 0.7;
    private static final double INSIDE_MAX_SINK = -0.3;
    private static final double TOP_RISE = 0.1;
    private static final double TOP_MAX_RISE = 1.8;
    private static final double TOP_MAX_SINK = -0.9;
    private static final double SINK = 0.03;

    /**
     * How far inside its box an entity starts going through a block.
     */
    private static final double MARGIN = 1.0E-5;

    /**
     * How many splashes and bubbles the top of a column throws up for each entity in it.
     */
    private static final int TOP_PARTICLES = 2;
    private static final float BUBBLE_RISE = 0.01f;
    private static final float SPLASH_SPEED = 1.0f;
    private static final float BUBBLE_SPEED = 0.2f;

    private BubbleColumnBlock() {
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        final BlockData below = world.blockAt(position.relative(BlockFace.DOWN));
        if (below != null) {
            refreshColumn(world, position, state, below);
        }
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (world.world() instanceof ServerWorld) {
            world.world().ticks().scheduleFluidTick(position, FluidType.WATER.fluidKey(), WATER_TICK_DELAY);
        }
        if (!canSurvive(world, position) || direction == BlockFace.DOWN
                || (direction == BlockFace.UP && !neighbor.key().equals(KEY) && columnFits(neighbor))) {
            world.scheduleTick(position, KEY, CHECK_DELAY);
        }
        return state;
    }

    private static boolean canSurvive(final RedstoneWorld world, final BlockPosition position) {
        final BlockData below = world.blockAt(position.relative(BlockFace.DOWN));
        return below != null && (below.key().equals(KEY) || enablesColumn(below));
    }

    /**
     * {@return {@code true} if a block below water makes a bubble column of it}
     */
    static boolean enablesColumn(final BlockData state) {
        return FluidBlock.isTagged(state, BlockTypeTagKeys.ENABLES_BUBBLE_COLUMN_PUSH_UP)
                || FluidBlock.isTagged(state, BlockTypeTagKeys.ENABLES_BUBBLE_COLUMN_DRAG_DOWN);
    }

    /**
     * Sets the column standing on a block: the block at {@code position} and every water source
     * above it turn into the column its base gives, or back into water if the base gives none.
     *
     * @param world    the world of the column
     * @param position where the column starts
     * @param occupied the block there
     * @param below    the block below it
     */
    static void refreshColumn(final RedstoneWorld world, final BlockPosition position, final BlockData occupied,
                              final BlockData below) {
        if (!columnFits(occupied)) {
            return;
        }
        final BlockData column = columnOver(below, occupied);
        if (column == null) {
            return;
        }
        world.setBlock(position, column, RedstoneWorld.Update.SHAPES);
        BlockPosition next = position.relative(BlockFace.UP);
        BlockData above = world.blockAt(next);
        while (above != null && columnFits(above)) {
            if (!world.setBlock(next, column, RedstoneWorld.Update.SHAPES)) {
                return;
            }
            next = next.relative(BlockFace.UP);
            above = world.blockAt(next);
        }
    }

    private static boolean columnFits(final BlockData state) {
        return state.key().equals(KEY) || FluidBlock.occupiedByBubbleColumn(state);
    }

    /**
     * {@return the block a column standing on {@code below} takes the place of {@code occupied}
     * with}: the column below carries on, a block of the tags starts a column, and without either a
     * column turns back into water
     */
    private static @Nullable BlockData columnOver(final BlockData below, final BlockData occupied) {
        if (below.key().equals(KEY)) {
            return below;
        }
        if (FluidBlock.isTagged(below, BlockTypeTagKeys.ENABLES_BUBBLE_COLUMN_PUSH_UP)) {
            return column(false);
        }
        if (FluidBlock.isTagged(below, BlockTypeTagKeys.ENABLES_BUBBLE_COLUMN_DRAG_DOWN)) {
            return column(true);
        }
        if (occupied.key().equals(KEY)) {
            final BlockType water = Blocks.type(BlockTypeKeys.WATER);
            return water == null ? null : water.defaultData();
        }
        return occupied;
    }

    private static @Nullable BlockData column(final boolean drag) {
        final BlockType type = Blocks.type(KEY);
        final BlockData data = type == null ? null : type.defaultData();
        return data == null ? null : data.with(DRAG, Boolean.toString(drag));
    }

    /**
     * Pushes an entity along the bubble columns its box goes through, once for each block of column.
     *
     * @param world     the world of the entity
     * @param minX      the lowest x of the box of the entity
     * @param minY      the lowest y of the box of the entity
     * @param minZ      the lowest z of the box of the entity
     * @param maxX      the highest x of the box of the entity
     * @param maxY      the highest y of the box of the entity
     * @param maxZ      the highest z of the box of the entity
     * @param velocityY the vertical speed of the entity
     * @return the vertical speed of the entity once pushed, {@code velocityY} if it is in no column
     */
    public static double push(final ServerWorld world, final double minX, final double minY, final double minZ,
                              final double maxX, final double maxY, final double maxZ, final double velocityY) {
        double speed = velocityY;
        for (int x = floor(minX + MARGIN); x <= floor(maxX - MARGIN); x++) {
            for (int z = floor(minZ + MARGIN); z <= floor(maxZ - MARGIN); z++) {
                for (int y = floor(minY + MARGIN); y <= floor(maxY - MARGIN); y++) {
                    final BlockState state = BlockView.blockAt(world, x, y, z);
                    if (state == null || !state.name().equals(KEY)) {
                        continue;
                    }
                    final boolean drag = Boolean.parseBoolean(state.properties().get(DRAG));
                    final BlockState above = BlockView.blockAt(world, x, y + 1, z);
                    if (above != null && BlockCollision.shape(above).isEmpty() && FluidBlockCodec.fromBlock(above).isEmpty()) {
                        speed = drag ? Math.max(TOP_MAX_SINK, speed - SINK) : Math.min(TOP_MAX_RISE, speed + TOP_RISE);
                        splash(world, x, y + 1, z);
                    } else {
                        speed = drag ? Math.max(INSIDE_MAX_SINK, speed - SINK) : Math.min(INSIDE_MAX_RISE, speed + INSIDE_RISE);
                    }
                }
            }
        }
        return speed;
    }

    /**
     * Throws up splashes and bubbles at the top of a column.
     */
    private static void splash(final ServerWorld world, final int x, final int y, final int z) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int index = 0; index < TOP_PARTICLES; index++) {
            particle(world, ParticleTypeKeys.SPLASH, x + random.nextDouble(), y, z + random.nextDouble(), 0.0f, SPLASH_SPEED);
            particle(world, ParticleTypeKeys.BUBBLE, x + random.nextDouble(), y, z + random.nextDouble(), BUBBLE_RISE, BUBBLE_SPEED);
        }
    }

    private static void particle(final ServerWorld world, final TypedKey<ParticleType> type, final double x, final double y,
                                 final double z, final float spreadY, final float speed) {
        FidorialServer.getInstance().players().broadcastNear(world, x, y, z,
                new ClientboundLevelParticlesPacket(type, x, y, z, 0.0f, spreadY, 0.0f, speed, 1), null);
    }

    private static int floor(final double value) {
        return (int) Math.floor(value);
    }

    /**
     * {@return {@code true} if a bubble column drags down what is in it}, {@code false} if it
     * pushes it up
     *
     * @param state a bubble column
     */
    public static boolean dragsDown(final BlockData state) {
        return Boolean.parseBoolean(state.get(DRAG));
    }
}
