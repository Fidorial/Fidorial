package fr.euphyllia.fidorial.server.world.fluid;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.debug.DebugGameEvents;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.util.threading.ThreadContexts;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.BlockSolidity;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.environment.Attribute;
import fr.fidorial.world.fluid.FluidManager;
import fr.fidorial.world.redstone.RedstoneWorld;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * The server's {@link FluidManager}: water and lava flowing on their fluid ticks.
 *
 * <p>On its tick, flowing fluid first works out its new amount: a full block falling when the same
 * fluid stands above it, a source when two sources stand beside it over a solid block or a source
 * (if the game rules let that fluid form sources), one step less than the fullest fluid beside it
 * otherwise, or nothing. Then it falls into the block below if it can, and only spreads sideways
 * when it cannot, or when three sources surround it: towards the nearest way down within a few
 * blocks, or every way when there is none. A source does the same without changing.</p>
 *
 * <p>Fluid goes into air and into the blocks of the {@code washed_away_by_fluids} tag, which break
 * and drop what they would drop. Water fills the waterloggable blocks it reaches from a source.
 * Fluids flow from a block into another only through faces their collision shapes leave open.
 * Water falling onto lava, or lava onto water, plays out as the game does: lava falling into water
 * turns it into stone, water takes the place of lava standing at least four ninths of a block
 * high.</p>
 *
 * <p>Writes go through the block updates of the redstone engine in effect, so that the blocks
 * around follow every change; ticks are scheduled in the {@linkplain World#ticks() ticks of the
 * world}.</p>
 */
public final class FluidEngine implements FluidManager {

    private static final int WATER_TICK_DELAY = 5;
    private static final int SLOW_LAVA_TICK_DELAY = 30;
    private static final int FAST_LAVA_TICK_DELAY = 10;
    private static final int SLOPE_REACH = 4;
    private static final int SLOW_LAVA_SLOPE_REACH = 2;
    /**
     * How far away a way down may be for fluid to spread towards it, beyond any slope: more than
     * any slope finding reaches.
     */
    private static final int NO_SLOPE = 1000;
    /**
     * How much falling fluid gives the blocks beside it, as if it held seven eighths.
     */
    private static final int FALLING_SPREAD = 7;
    /**
     * How many sources around falling fluid make it spread sideways as well.
     */
    private static final int SOURCES_TO_SPREAD_WHILE_FALLING = 3;
    /**
     * How high lava stands, at least, for water to take its place.
     */
    private static final float LAVA_HEIGHT_REPLACED_BY_WATER = 0.44444445f;
    /**
     * The level event of fluid hissing as it hardens: smoke and a sizzle.
     */
    private static final int HISS_EVENT = 1501;
    /**
     * Lava spreading further by flowing takes four times as long, three times out of four.
     */
    private static final int SLOW_SPREAD_FACTOR = 4;

    /**
     * The sides fluid looks around for neighbours and slopes.
     */
    private static final BlockFace[] HORIZONTAL = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    /**
     * The blocks always full of water that take no other fluid.
     */
    private static final Set<TypedKey<BlockType>> WATER_PLANTS = Set.of(
            BlockTypeKeys.KELP, BlockTypeKeys.KELP_PLANT, BlockTypeKeys.SEAGRASS, BlockTypeKeys.TALL_SEAGRASS);

    private static final BlockState AIR = BlockState.defaultOf(BlockTypeKeys.AIR);
    /**
     * What an unloaded block counts as: a wall fluid does not go through.
     */
    private static final BlockState UNLOADED = BlockState.defaultOf(BlockTypeKeys.BARRIER);

    private final ThreadedRegionRegionizer regionizer;
    private final FluidCurrents currents;
    private final Function<ServerWorld, RedstoneWorld> updates;
    private volatile @Nullable DebugGameEvents debugEvents;

    /**
     * @param regionizer the regions the blocks belong to
     * @param updates    the block updates of a world, as the redstone engine in effect runs them
     */
    public FluidEngine(final ThreadedRegionRegionizer regionizer, final Function<ServerWorld, RedstoneWorld> updates) {
        this.regionizer = regionizer;
        this.currents = new FluidCurrents();
        this.updates = updates;
    }

    public void setDebugEvents(final DebugGameEvents debugEvents) {
        this.debugEvents = debugEvents;
    }

    /**
     * {@return how the currents push the entities in the fluids}
     */
    public FluidCurrents currents() {
        return currents;
    }


    /**
     * {@return how many game ticks pass between two ticks of a fluid}: five for water, thirty for
     * lava, ten where lava is fast
     */
    public static int tickDelay(final ServerWorld world, final FluidType type) {
        if (type == FluidType.WATER) {
            return WATER_TICK_DELAY;
        }
        return isFastLava(world) ? FAST_LAVA_TICK_DELAY : SLOW_LAVA_TICK_DELAY;
    }

    private static int levelDrop(final ServerWorld world, final FluidType type) {
        return type == FluidType.LAVA && !isFastLava(world) ? 2 : 1;
    }

    private static int slopeReach(final ServerWorld world, final FluidType type) {
        return type == FluidType.LAVA && !isFastLava(world) ? SLOW_LAVA_SLOPE_REACH : SLOPE_REACH;
    }

    private static boolean formsSources(final ServerWorld world, final FluidType type) {
        return world.gameRuleValues().getBoolean(type == FluidType.WATER
                ? GameRuleKeys.WATER_SOURCE_CONVERSION : GameRuleKeys.LAVA_SOURCE_CONVERSION);
    }

    /**
     * {@return {@code true} if lava is fast in a world}, as in the Nether: it spreads further and
     * three times as often
     */
    public static boolean isFastLava(final ServerWorld world) {
        final Attribute<Boolean> fastLava = world.dimensionType().attributes().fastLava();
        return fastLava != null && Boolean.TRUE.equals(fastLava.value());
    }


    @Override
    public FluidStateImpl fluidAt(final World world, final BlockPosition position) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            return FluidStateImpl.empty();
        }
        return FluidBlockCodec.fromBlock(BlockView.blockAt(serverWorld, position.blockX(), position.blockY(), position.blockZ()));
    }

    @Override
    public boolean placeSource(final World world, final BlockPosition position, final TypedKey<Fluid> fluid) {
        Preconditions.checkArgument(fluid != null, "The fluid passed to FluidManager.placeSource() must not be null");
        final FluidType type = FluidType.byFluidKey(fluid);
        Preconditions.checkArgument(type != null, "Only water and lava can be placed, not %s", fluid);
        return placeSource(world, position, type);
    }

    /**
     * Places a source of fluid, as {@link #placeSource(World, BlockPosition, TypedKey)} does.
     *
     * @param world    the world
     * @param position the block
     * @param type     the fluid
     * @return {@code true} if the source was placed
     */
    public boolean placeSource(final World world, final BlockPosition position, final FluidType type) {
        Preconditions.checkArgument(world instanceof ServerWorld, "The fluids only flow in the worlds of the server, not %s", world);
        Preconditions.checkArgument(position != null, "The position passed to FluidManager.placeSource() must not be null");
        final ServerWorld serverWorld = (ServerWorld) world;
        ThreadContexts.checkOwnedByCurrentThread(world, position, "placeSource");
        final BlockState there = BlockView.blockAt(serverWorld, position.blockX(), position.blockY(), position.blockZ());
        if (there == null) {
            return false;
        }
        final RedstoneWorld view = updates.apply(serverWorld);
        if (type == FluidType.WATER && isWaterloggable(there)) {
            if (there.isWaterlogged() || !setBlock(view, position, withWaterlogged(there, true))) {
                return false;
            }
            world.ticks().scheduleFluidTick(position, type.fluidKey(), tickDelay(serverWorld, type));
            gameEvent(serverWorld, GameEventKeys.FLUID_PLACE, position);
            return true;
        }
        final FluidStateImpl current = FluidBlockCodec.fromBlock(there);
        if (current.type() == type && current.isSource()) {
            return false;
        }
        if (!there.isAir() && !there.isTagged(BlockTypeTagKeys.REPLACEABLE) && BlockSolidity.isSolid(there)) {
            return false; // a bucket does not empty into a wall
        }
        if (!there.isAir() && !there.isFluid()) {
            view.destroyBlock(position);
        }
        if (!setBlock(view, position, FluidBlockCodec.toBlock(FluidStateImpl.source(type)))) {
            return false;
        }
        gameEvent(serverWorld, GameEventKeys.FLUID_PLACE, position);
        return true;
    }

    @Override
    public boolean removeFluid(final World world, final BlockPosition position) {
        Preconditions.checkArgument(world instanceof ServerWorld, "The fluids only flow in the worlds of the server, not %s", world);
        Preconditions.checkArgument(position != null, "The position passed to FluidManager.removeFluid() must not be null");
        final ServerWorld serverWorld = (ServerWorld) world;
        ThreadContexts.checkOwnedByCurrentThread(world, position, "removeFluid");
        final BlockState there = BlockView.blockAt(serverWorld, position.blockX(), position.blockY(), position.blockZ());
        if (there == null) {
            return false;
        }
        final RedstoneWorld view = updates.apply(serverWorld);
        final boolean taken;
        if (there.isWaterlogged()) {
            taken = setBlock(view, position, withWaterlogged(there, false));
        } else if (there.name().equals(BlockTypeKeys.BUBBLE_COLUMN)
                || (FluidType.byBlockKey(there.name()) != null && FluidBlockCodec.fromBlock(there).isSource())) {
            taken = setBlock(view, position, AIR);
        } else {
            taken = false; // flowing fluid, or a plant always under water, cannot be taken
        }
        if (taken) {
            gameEvent(serverWorld, GameEventKeys.FLUID_PICKUP, position);
        }
        return taken;
    }

    @Override
    public void scheduleUpdate(final World world, final BlockPosition position) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            return;
        }
        final FluidType type = fluidAt(world, position).type();
        if (type != null) {
            world.ticks().scheduleFluidTick(position, type.fluidKey(), tickDelay(serverWorld, type));
        }
    }


    /**
     * Runs a due fluid tick.
     *
     * @param world    the world of the fluid
     * @param position where it is, owned by the calling thread
     * @param type     the fluid the tick is for: nothing happens if the block holds another one
     */
    public void tick(final ServerWorld world, final BlockPosition position, final FluidType type) {
        new Flow(world, type).tick(position);
    }

    /**
     * The flow of one fluid in one world, for one tick.
     */
    private final class Flow {

        private final ServerWorld world;
        private final FluidType type;
        private final RedstoneWorld view;
        private final int levelDrop;

        Flow(final ServerWorld world, final FluidType type) {
            this.world = world;
            this.type = type;
            this.view = updates.apply(world);
            this.levelDrop = FluidEngine.levelDrop(world, type);
        }

        void tick(final BlockPosition position) {
            BlockState state = stateAt(position);
            FluidStateImpl fluid = FluidBlockCodec.fromBlock(state);
            if (fluid.type() != type) {
                return;
            }
            if (!fluid.isSource()) {
                final FluidStateImpl updated = newFluid(position, state);
                final int delay = flowDelay(position, fluid, updated);
                if (updated.isEmpty()) {
                    fluid = updated;
                    state = AIR;
                    setBlock(view, position, state);
                } else if (!updated.equals(fluid)) {
                    fluid = updated;
                    state = FluidBlockCodec.toBlock(updated);
                    setBlock(view, position, state);
                    world.ticks().scheduleFluidTick(position, type.fluidKey(), delay);
                }
            }
            flowOn(position, state, fluid);
        }

        /**
         * {@return in how many game ticks fluid that changed ticks again}: lava rising in its block
         * takes four times as long, three times out of four
         */
        private int flowDelay(final BlockPosition position, final FluidStateImpl before, final FluidStateImpl after) {
            final int delay = tickDelay(world, type);
            if (type == FluidType.LAVA && !before.isEmpty() && !after.isEmpty() && !before.isFalling() && !after.isFalling()
                    && height(position, after) > height(position, before)
                    && ThreadLocalRandom.current().nextInt(SLOW_SPREAD_FACTOR) != 0) {
                return delay * SLOW_SPREAD_FACTOR;
            }
            return delay;
        }

        private void flowOn(final BlockPosition position, final BlockState state, final FluidStateImpl fluid) {
            if (fluid.isEmpty()) {
                return;
            }
            final BlockPosition below = position.relative(BlockFace.DOWN);
            final BlockState belowState = stateAt(below);
            final FluidStateImpl belowFluid = FluidBlockCodec.fromBlock(belowState);
            if (mayFlowInto(state, BlockFace.DOWN, belowState, belowFluid)) {
                final FluidStateImpl fallen = newFluid(below, belowState);
                if (givesWayTo(belowFluid, below, fallen, BlockFace.DOWN) && takesFluid(belowState, fallen)) {
                    flowInto(below, belowState, BlockFace.DOWN, fallen);
                    if (sourcesAround(position) >= SOURCES_TO_SPREAD_WHILE_FALLING) {
                        flowSideways(position, fluid, state);
                    }
                    return;
                }
            }
            if (fluid.isSource() || !isWayDown(state, belowState)) {
                flowSideways(position, fluid, state);
            }
        }

        private void flowSideways(final BlockPosition position, final FluidStateImpl fluid, final BlockState state) {
            final int given = fluid.isFalling() ? FALLING_SPREAD : fluid.amount() - levelDrop;
            if (given <= 0) {
                return;
            }
            for (final Map.Entry<BlockFace, FluidStateImpl> entry : sidewaysFlows(position, state).entrySet()) {
                final BlockPosition target = position.relative(entry.getKey());
                flowInto(target, stateAt(target), entry.getKey(), entry.getValue());
            }
        }

        /**
         * {@return the fluid a block would hold from the fluid around it}
         */
        private FluidStateImpl newFluid(final BlockPosition position, final BlockState state) {
            int fullest = 0;
            int sources = 0;
            for (final BlockFace face : HORIZONTAL) {
                final BlockPosition beside = position.relative(face);
                final BlockState besideState = stateAt(beside);
                final FluidStateImpl besideFluid = FluidBlockCodec.fromBlock(besideState);
                if (besideFluid.type() == type && isOpenBetween(face, state, besideState)) {
                    if (besideFluid.isSource()) {
                        sources++;
                    }
                    fullest = Math.max(fullest, besideFluid.amount());
                }
            }
            if (sources >= 2 && formsSources(world, type)) {
                final BlockState belowState = stateAt(position.relative(BlockFace.DOWN));
                final FluidStateImpl belowFluid = FluidBlockCodec.fromBlock(belowState);
                if (BlockSolidity.isSolid(belowState) || isOwnSource(belowFluid)) {
                    return FluidStateImpl.source(type);
                }
            }
            final BlockState aboveState = stateAt(position.relative(BlockFace.UP));
            final FluidStateImpl aboveFluid = FluidBlockCodec.fromBlock(aboveState);
            if (aboveFluid.type() == type && isOpenBetween(BlockFace.UP, state, aboveState)) {
                return FluidStateImpl.falling(type);
            }
            final int amount = fullest - levelDrop;
            return amount <= 0 ? FluidStateImpl.empty() : FluidStateImpl.flowing(type, amount);
        }

        /**
         * {@return the sides fluid spreads to, and the fluid it puts there}: those leading the
         * soonest to a way down, all those it can flow to when none does
         */
        private Map<BlockFace, FluidStateImpl> sidewaysFlows(final BlockPosition position, final BlockState state) {
            int nearest = NO_SLOPE;
            final Map<BlockFace, FluidStateImpl> result = new EnumMap<>(BlockFace.class);
            Slopes slopes = null;
            for (final BlockFace face : HORIZONTAL) {
                final BlockPosition target = position.relative(face);
                final BlockState targetState = stateAt(target);
                final FluidStateImpl targetFluid = FluidBlockCodec.fromBlock(targetState);
                if (!mayFlowInto(state, face, targetState, targetFluid)) {
                    continue;
                }
                final FluidStateImpl spread = newFluid(target, targetState);
                if (!takesFluid(targetState, spread)) {
                    continue;
                }
                if (slopes == null) {
                    slopes = new Slopes(position);
                }
                final int distance = slopes.isWayDown(target) ? 0 : slopes.distance(target, 1, face.opposite(), targetState);
                if (distance < nearest) {
                    result.clear();
                }
                if (distance <= nearest) {
                    if (givesWayTo(targetFluid, target, spread, face)) {
                        result.put(face, spread);
                    }
                    nearest = distance;
                }
            }
            return result;
        }

        private void flowInto(final BlockPosition position, final BlockState state, final BlockFace direction,
                              final FluidStateImpl target) {
            if (!regionizer.isOwnedByCurrentThread(world.key(), position.chunk())) {
                regionizer.execute(world.key(), position.chunk(), () -> flowInto(position, stateAt(position), direction, target));
                return;
            }
            if (type == FluidType.LAVA && direction == BlockFace.DOWN && FluidBlockCodec.fromBlock(state).type() == FluidType.WATER) {
                // lava falling into water hardens it
                if (state.name().equals(BlockTypeKeys.WATER)) {
                    setBlock(view, position, BlockState.defaultOf(BlockTypeKeys.STONE));
                }
                hiss(world, position);
                return;
            }
            if (isFluidHolder(state)) {
                if (isWaterloggable(state) && !state.isWaterlogged() && target.isSource() && target.type() == FluidType.WATER
                        && setBlock(view, position, withWaterlogged(state, true))) {
                    world.ticks().scheduleFluidTick(position, FluidType.WATER.fluidKey(), WATER_TICK_DELAY);
                }
                return;
            }
            if (!state.isAir()) {
                if (type == FluidType.LAVA) {
                    hiss(world, position);
                }
                view.destroyBlock(position);
            }
            setBlock(view, position, FluidBlockCodec.toBlock(target));
        }

        private int sourcesAround(final BlockPosition position) {
            int sources = 0;
            for (final BlockFace face : HORIZONTAL) {
                if (isOwnSource(FluidBlockCodec.fromBlock(stateAt(position.relative(face))))) {
                    sources++;
                }
            }
            return sources;
        }

        /**
         * {@return {@code true} if fluid can go down from a block into the one below}: falling into
         * the same fluid, or into a block it can take the place of
         */
        private boolean isWayDown(final BlockState topState, final BlockState bottomState) {
            if (!isOpenBetween(BlockFace.DOWN, topState, bottomState)) {
                return false;
            }
            return FluidBlockCodec.fromBlock(bottomState).type() == type
                    || (takesAnyFluid(bottomState) && takesFluid(bottomState, FluidStateImpl.falling(type)));
        }

        private boolean flowsInto(final BlockState sourceState, final BlockFace direction, final BlockState testState) {
            return mayFlowInto(sourceState, direction, testState, FluidBlockCodec.fromBlock(testState))
                    && takesFluid(testState, FluidStateImpl.falling(type));
        }

        private boolean mayFlowInto(final BlockState sourceState, final BlockFace direction, final BlockState testState,
                                    final FluidStateImpl testFluid) {
            return !isOwnSource(testFluid) && takesAnyFluid(testState) && isOpenBetween(direction, sourceState, testState);
        }

        private boolean isOwnSource(final FluidStateImpl fluid) {
            return fluid.type() == type && fluid.isSource();
        }

        /**
         * {@return {@code true} if fluid of this type may take the place of the fluid of a block}:
         * any fluid replaces no fluid, only fluid falling from above replaces water and only if it
         * is not water, and only water replaces lava standing high enough
         */
        private boolean givesWayTo(final FluidStateImpl fluid, final BlockPosition position, final FluidStateImpl other,
                                   final BlockFace direction) {
            final FluidType current = fluid.type();
            if (current == null) {
                return true;
            }
            if (current == FluidType.WATER) {
                return direction == BlockFace.DOWN && other.type() != FluidType.WATER;
            }
            return height(position, fluid) >= LAVA_HEIGHT_REPLACED_BY_WATER && other.type() == FluidType.WATER;
        }

        /**
         * {@return how high a fluid stands in a block}: whole when the same fluid stands above
         */
        private float height(final BlockPosition position, final FluidStateImpl fluid) {
            final FluidStateImpl above = FluidBlockCodec.fromBlock(stateAt(position.relative(BlockFace.UP)));
            return fluid.type() != null && above.type() == fluid.type() ? 1.0f : fluid.ownHeight();
        }

        private BlockState stateAt(final BlockPosition position) {
            final BlockState state = BlockView.blockAt(world, position.blockX(), position.blockY(), position.blockZ());
            return state == null ? UNLOADED : state;
        }

        /**
         * The ways down around a block fluid spreads from, worked out once for its tick.
         */
        private final class Slopes {

            private final BlockPosition origin;
            private final Map<Integer, BlockState> states = new HashMap<>();
            private final Map<Integer, Boolean> holes = new HashMap<>();

            Slopes(final BlockPosition origin) {
                this.origin = origin;
            }

            BlockState stateAt(final BlockPosition position) {
                return states.computeIfAbsent(key(position), _ -> Flow.this.stateAt(position));
            }

            boolean isWayDown(final BlockPosition position) {
                return holes.computeIfAbsent(key(position), _ -> {
                    final BlockPosition below = position.relative(BlockFace.DOWN);
                    return Flow.this.isWayDown(stateAt(position), Flow.this.stateAt(below));
                });
            }

            /**
             * {@return how many blocks away the nearest way down is, {@link #NO_SLOPE} for none
             * within reach}, never going back the way it came
             */
            int distance(final BlockPosition position, final int pass, final BlockFace from, final BlockState state) {
                int nearest = NO_SLOPE;
                for (final BlockFace face : HORIZONTAL) {
                    if (face == from) {
                        continue;
                    }
                    final BlockPosition test = position.relative(face);
                    final BlockState testState = stateAt(test);
                    if (!flowsInto(state, face, testState)) {
                        continue;
                    }
                    if (isWayDown(test)) {
                        return pass;
                    }
                    if (pass < slopeReach(world, type)) {
                        nearest = Math.min(nearest, distance(test, pass + 1, face.opposite(), testState));
                    }
                }
                return nearest;
            }

            private int key(final BlockPosition position) {
                return ((position.blockX() - origin.blockX() + 128) & 0xFF) << 8 | ((position.blockZ() - origin.blockZ() + 128) & 0xFF);
            }
        }
    }


    /**
     * {@return {@code true} if a block takes fluid in some way}: a waterloggable block, a plant
     * always under water, or a block fluids wash away
     */
    static boolean takesAnyFluid(final BlockState state) {
        return isFluidHolder(state) || state.isTagged(BlockTypeTagKeys.WASHED_AWAY_BY_FLUIDS);
    }

    /**
     * {@return {@code true} unless a block holding fluid in its own way refuses a fluid}: a
     * waterloggable block only takes a source of water, a plant always under water takes nothing
     */
    static boolean takesFluid(final BlockState state, final FluidStateImpl fluid) {
        if (isWaterloggable(state)) {
            return fluid.isSource() && fluid.type() == FluidType.WATER;
        }
        return !WATER_PLANTS.contains(state.name());
    }

    /**
     * {@return {@code true} if a block holds water of its own, which wakes up whenever a block next
     * to it changes shape}: a waterlogged block, or a plant always under water
     *
     * @param state the block
     */
    public static boolean holdsOwnWater(final BlockState state) {
        return state.isWaterlogged() || WATER_PLANTS.contains(state.name());
    }

    private static boolean isFluidHolder(final BlockState state) {
        return isWaterloggable(state) || WATER_PLANTS.contains(state.name());
    }

    static boolean isWaterloggable(final BlockState state) {
        return state.has(BlockProperties.WATERLOGGED);
    }

    private static BlockState withWaterlogged(final BlockState state, final boolean waterlogged) {
        return state.with(BlockProperties.WATERLOGGED, waterlogged);
    }

    /**
     * {@return {@code true} if fluid flows from a block into the one beside it}: neither is a full
     * block, and the faces they show each other leave an opening
     */
    static boolean isOpenBetween(final BlockFace direction, final BlockState source, final BlockState target) {
        final CollisionShape targetShape = BlockCollision.shape(target);
        if (targetShape.isFullBlock()) {
            return false;
        }
        final CollisionShape sourceShape = BlockCollision.shape(source);
        if (sourceShape.isFullBlock()) {
            return false;
        }
        if (sourceShape.isEmpty() && targetShape.isEmpty()) {
            return true;
        }
        return !FaceOcclusion.sealsFace(sourceShape, targetShape, direction);
    }

    private boolean setBlock(final RedstoneWorld view, final BlockPosition position, final BlockState state) {
        return view.setBlock(position, state.data(), RedstoneWorld.Update.NEIGHBORS);
    }

    /**
     * Plays fluid hissing at a block: lava meeting water, or burning a block it flows into.
     */
    static void hiss(final ServerWorld world, final BlockPosition position) {
        world.sendToChunkViewers(position.chunkX(), position.chunkZ(), new ClientboundLevelEventPacket(HISS_EVENT, position, 0, false));
    }

    private void gameEvent(final ServerWorld world, final TypedKey<GameEvent> event, final BlockPosition position) {
        final DebugGameEvents events = debugEvents;
        if (events != null) {
            events.emit(world, event, position);
        }
    }
}
