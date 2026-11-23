package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.FluidTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import org.jspecify.annotations.Nullable;

/**
 * Water and lava as blocks: what wakes their fluid up, and what they make of what they touch.
 *
 * <p>A fluid block placed, or one whose neighbour changed, schedules a tick of its fluid, and so
 * does one next to which a block changed shape when either of them holds a source. Before that,
 * lava touching water from above or from a side hardens, into obsidian for a source and into
 * cobblestone otherwise; lava above soul soil touching blue ice turns into basalt; water touching
 * blue ice from a side turns into brittle ice.</p>
 *
 * <p>A water source above a block of the {@code enables_bubble_column_push_up} or
 * {@code enables_bubble_column_drag_down} tags turns into a bubble column a second later.</p>
 */
public final class FluidBlock implements RedstoneBehaviour {

    public static final FluidBlock WATER = new FluidBlock(FluidType.WATER);
    public static final FluidBlock LAVA = new FluidBlock(FluidType.LAVA);

    /**
     * How long a water source waits before turning into a bubble column, in game ticks.
     */
    private static final int BUBBLE_COLUMN_DELAY = 20;
    /**
     * The sides lava looks for water on: above and around, never below.
     */
    private static final BlockFace[] LAVA_CONTACTS = {BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
    private static final BlockFace[] WATER_CONTACTS = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};

    private final FluidType type;

    private FluidBlock(final FluidType type) {
        this.type = type;
    }

    /**
     * {@return the block type of this fluid}
     */
    public TypedKey<BlockType> key() {
        return type.blockKey();
    }

    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        wake(world, position, state);
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        wake(world, position, state);
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (fluidOf(state).isSource() || fluidOf(neighbor).isSource()) {
            scheduleFluidTick(world, position);
        }
        if (direction == BlockFace.DOWN && occupiedByBubbleColumn(state)) {
            scheduleBubbleColumn(world, position, neighbor);
        }
        return state;
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (occupiedByBubbleColumn(state)) {
            final BlockData below = world.blockAt(position.relative(BlockFace.DOWN));
            if (below != null) {
                BubbleColumnBlock.refreshColumn(world, position, state, below);
            }
        }
    }

    /**
     * Lets the fluid of the block react to what it touches, then flow on.
     */
    private void wake(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (stillFlows(world, position)) {
            scheduleFluidTick(world, position);
        }
        if (occupiedByBubbleColumn(state)) {
            final BlockData below = world.blockAt(position.relative(BlockFace.DOWN));
            if (below != null) {
                scheduleBubbleColumn(world, position, below);
            }
        }
    }

    private void scheduleFluidTick(final RedstoneWorld world, final BlockPosition position) {
        if (world.world() instanceof final ServerWorld serverWorld) {
            world.world().ticks().scheduleFluidTick(position, type.fluidKey(), FluidEngine.tickDelay(serverWorld, type));
        }
    }

    private void scheduleBubbleColumn(final RedstoneWorld world, final BlockPosition position, final BlockData below) {
        if (BubbleColumnBlock.enablesColumn(below)) {
            world.scheduleTick(position, key(), BUBBLE_COLUMN_DELAY);
        }
    }

    /**
     * {@return {@code false} if the fluid turned into a block on touching another one}, and no
     * longer flows
     */
    private boolean stillFlows(final RedstoneWorld world, final BlockPosition position) {
        if (type == FluidType.LAVA) {
            final boolean overSoulSoil = isBlock(world.blockAt(position.relative(BlockFace.DOWN)), BlockTypeKeys.SOUL_SOIL);
            for (final BlockFace face : LAVA_CONTACTS) {
                final BlockData touched = world.blockAt(position.relative(face));
                if (touched != null && fluidOf(touched).type() == FluidType.WATER) {
                    final boolean source = fluidOf(world.blockAt(position)).isSource();
                    harden(world, position, source ? BlockTypeKeys.OBSIDIAN : BlockTypeKeys.COBBLESTONE);
                    return false;
                }
                if (overSoulSoil && isBlock(touched, BlockTypeKeys.BLUE_ICE)) {
                    harden(world, position, BlockTypeKeys.BASALT);
                    return false;
                }
            }
        } else {
            for (final BlockFace face : WATER_CONTACTS) {
                if (isBlock(world.blockAt(position.relative(face)), BlockTypeKeys.BLUE_ICE)) {
                    setDefault(world, position, BlockTypeKeys.BRITTLE_ICE);
                    return false;
                }
            }
        }
        return true;
    }

    private static void harden(final RedstoneWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        setDefault(world, position, block);
        if (world.world() instanceof final ServerWorld serverWorld) {
            FluidEngine.hiss(serverWorld, position);
        }
    }

    private static void setDefault(final RedstoneWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final BlockType blockType = Blocks.type(block);
        final BlockData data = blockType == null ? null : blockType.defaultData();
        if (data != null) {
            world.setBlock(position, data, RedstoneWorld.Update.NEIGHBORS);
        }
    }

    private static boolean isBlock(final @Nullable BlockData state, final TypedKey<BlockType> block) {
        return state != null && state.key().equals(block);
    }

    /**
     * {@return {@code true} if a block is a fluid source a bubble column may take the place of}: a
     * source of a fluid of the {@code bubble_column_can_occupy} tag
     */
    static boolean occupiedByBubbleColumn(final BlockData state) {
        final FluidStateImpl fluid = fluidOf(state);
        final FluidType fluidType = fluid.type();
        return fluid.isSource() && fluidType != null && FluidType.byBlockKey(state.key()) == fluidType
                && FidorialServer.isInitialized()
                && FidorialServer.getInstance().registries().registry(RegistryKey.FLUID)
                .isTagged(fluidType.fluidKey(), FluidTagKeys.BUBBLE_COLUMN_CAN_OCCUPY);
    }

    static FluidStateImpl fluidOf(final @Nullable BlockData state) {
        return state == null ? FluidStateImpl.empty() : FluidBlockCodec.fromBlock(BlockState.withProperties(state.key(), state.propertyMap()));
    }

    static boolean isTagged(final BlockData state, final TagKey<BlockType> tag) {
        return BlockState.withProperties(state.key(), state.propertyMap()).isTagged(tag);
    }
}
