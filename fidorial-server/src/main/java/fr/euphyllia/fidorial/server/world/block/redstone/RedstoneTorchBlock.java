package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.redstone.Faces;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Lightable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import fr.fidorial.world.redstone.SupportType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The redstone torch, standing or on a wall, a port of vanilla's {@code RedstoneTorchBlock} and
 * {@code RedstoneWallTorchBlock}: it turns off when the block it is attached to is powered, two
 * game ticks later, and burns out when it toggles too often.
 */
public final class RedstoneTorchBlock implements BlockBehaviour, RedstoneBehaviour {

    public static final RedstoneTorchBlock STANDING = new RedstoneTorchBlock(BlockTypeKeys.REDSTONE_TORCH, false);

    public static final RedstoneTorchBlock WALL = new RedstoneTorchBlock(BlockTypeKeys.REDSTONE_WALL_TORCH, true);

    /**
     * The light a lit redstone torch gives.
     */
    public static final int LIGHT_EMISSION = 7;

    private static final int TOGGLE_DELAY = 2;
    private static final long RECENT_TOGGLE_TIMER = 60L;
    private static final int MAX_RECENT_TOGGLES = 8;
    private static final int RESTART_DELAY = 160;
    private static final int BURNOUT_EVENT = 1502;

    /**
     * Vanilla's {@code RECENT_TOGGLES}, per torch: when it turned off lately, oldest first. An array
     * is never changed once in the map, so that any thread may drop those that expired.
     */
    private static final Map<At, long[]> RECENT_TOGGLES = new ConcurrentHashMap<>();
    private static final long[] NO_TOGGLES = new long[0];
    private static final int MIN_PURGE_SIZE = 256;

    /**
     * How many torches may have toggled lately before those that no longer tick are dropped.
     */
    private static volatile int purgeSize = MIN_PURGE_SIZE;

    private final TypedKey<BlockType> key;
    private final boolean wall;

    private RedstoneTorchBlock(final TypedKey<BlockType> key, final boolean wall) {
        this.key = key;
        this.wall = wall;
    }

    private static @Nullable BlockData standingPlacement(final BlockPlaceContext context) {
        return canStandOn(context.relative(BlockFace.DOWN)) ? Redstone.defaultState(BlockTypeKeys.REDSTONE_TORCH) : null;
    }

    private static @Nullable BlockData wallPlacement(final BlockPlaceContext context) {
        for (final BlockFace direction : Placement.nearestLookingDirections(context)) {
            if (Faces.isHorizontal(direction) && Placement.canAttach(context, direction)) {
                return ((Directional) Redstone.defaultState(BlockTypeKeys.REDSTONE_WALL_TORCH)).withFacing(direction.opposite());
            }
        }
        return null;
    }

    private static int ownSignal(final BlockData state) {
        return state instanceof final Lightable lightable && lightable.isLit() ? 15 : 0;
    }

    /**
     * {@return when the torch turned off within the last three seconds, after dropping the older
     * times}
     */
    private static long[] recentToggles(final At at, final long now) {
        final long[] toggles = RECENT_TOGGLES.get(at);
        if (toggles == null) {
            return NO_TOGGLES;
        }
        int expired = 0;
        while (expired < toggles.length && now - toggles[expired] > RECENT_TOGGLE_TIMER) {
            expired++;
        }
        if (expired == 0) {
            return toggles;
        }
        final long[] recent = Arrays.copyOfRange(toggles, expired, toggles.length);
        if (recent.length == 0) {
            RECENT_TOGGLES.remove(at, toggles);
        } else {
            RECENT_TOGGLES.replace(at, toggles, recent);
        }
        return recent;
    }

    /**
     * Records the torch turning off.
     *
     * @return how many times it turned off within the last three seconds, this time included
     */
    private static int addToggle(final At at, final long[] recent, final long now) {
        final long[] toggles = Arrays.copyOf(recent, recent.length + 1);
        toggles[recent.length] = now;
        RECENT_TOGGLES.put(at, toggles);
        final int size = RECENT_TOGGLES.size();
        if (size > purgeSize) {
            // torches that stopped toggling never tick again to drop their times: drop them here
            RECENT_TOGGLES.entrySet().removeIf(entry -> entry.getKey().world().equals(at.world())
                    && now - entry.getValue()[entry.getValue().length - 1] > RECENT_TOGGLE_TIMER);
            purgeSize = Math.max(MIN_PURGE_SIZE, RECENT_TOGGLES.size() * 2);
        }
        return toggles.length;
    }

    /**
     * {@return vanilla's {@code canSupportCenter} on the top face}
     */
    private static boolean canStandOn(final @Nullable BlockData below) {
        return BlockSupport.isFaceSturdy(below, BlockFace.UP, SupportType.CENTER);
    }

    @Override
    public TypedKey<BlockType> key() {
        return key;
    }

    @Override
    public BlockType type() {
        return Redstone.type(key);
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData wallState = wallPlacement(context);
        for (final BlockFace direction : Placement.nearestLookingDirections(context)) {
            if (direction == BlockFace.UP) {
                continue; // a torch never hangs under a block
            }
            final BlockData candidate = direction == BlockFace.DOWN ? standingPlacement(context) : wallState;
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return data instanceof final Lightable lightable && lightable.isLit() ? LIGHT_EMISSION : 0;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return true;
    }

    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        // a torch never powers the block it is attached to
        return direction == supportSide(state).opposite() ? 0 : ownSignal(state);
    }

    @Override
    public int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        // strongly powers the block above it
        return direction == BlockFace.DOWN ? signal(world, position, state, direction) : 0;
    }

    private BlockFace supportSide(final BlockData state) {
        return wall && state instanceof final Directional directional ? directional.getFacing().opposite() : BlockFace.DOWN;
    }

    private boolean hasNeighborSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        final BlockFace support = supportSide(state);
        return world.hasSignal(position.relative(support), support);
    }

    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        notifyNeighbors(world, position);
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        notifyNeighbors(world, position);
    }

    private void notifyNeighbors(final RedstoneWorld world, final BlockPosition position) {
        for (final BlockFace face : Faces.ALL) {
            world.updateNeighbors(position.relative(face), key);
        }
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (state instanceof final Lightable lightable && lightable.isLit() == hasNeighborSignal(world, position, state)
                && !world.willTickThisTick(position, key)) {
            world.scheduleTick(position, key, TOGGLE_DELAY);
        }
    }

    /**
     * A lit torch whose support is powered turns off, and burns out if it did so eight
     * times within three seconds, to try again eight seconds later; an unlit one turns back on.
     */
    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Lightable lightable)) {
            return;
        }
        final boolean powered = hasNeighborSignal(world, position, state);
        final At at = new At(world.world().key(), position);
        final long now = world.world().dayNightCycle().worldAge();
        final long[] recent = recentToggles(at, now);
        if (lightable.isLit()) {
            if (powered) {
                world.setBlock(position, lightable.withLit(false), RedstoneWorld.Update.NEIGHBORS);
                if (addToggle(at, recent, now) >= MAX_RECENT_TOGGLES) {
                    Redstone.levelEvent(world.world(), BURNOUT_EVENT, position, 0);
                    final BlockData current = world.blockAt(position);
                    if (current != null) {
                        world.scheduleTick(position, current.key(), RESTART_DELAY);
                    }
                }
            }
        } else if (!powered && recent.length < MAX_RECENT_TOGGLES) {
            world.setBlock(position, lightable.withLit(true), RedstoneWorld.Update.NEIGHBORS);
        }
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        final BlockFace support = supportSide(state);
        if (direction != support) {
            return state;
        }
        final boolean holds = wall
                ? BlockSupport.isFaceSturdy(neighbor, support.opposite(), SupportType.FULL)
                : canStandOn(neighbor);
        return holds ? state : Redstone.air();
    }

    private record At(Key world, BlockPosition position) {
    }
}
