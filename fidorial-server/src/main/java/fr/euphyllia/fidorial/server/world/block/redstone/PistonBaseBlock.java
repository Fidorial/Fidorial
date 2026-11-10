package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.redstone.Faces;
import fr.euphyllia.fidorial.server.world.redstone.MovingPistons;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.type.Piston;
import fr.fidorial.world.block.data.type.PistonHead;
import fr.fidorial.world.block.data.type.TechnicalPiston;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The base of a piston, sticky or not.
 *
 * <p>A powered piston extends: its head comes out and pushes up to twelve blocks, breaking those
 * in the way that do not withstand it. Once it is no longer powered it retracts, and a sticky one
 * pulls the block in front of its head back, with whatever slime and honey blocks drag along.</p>
 *
 * <p>Three things of vanilla are kept as they are, since contraptions rely on them:</p>
 * <ul>
 *   <li>a piston is powered through any side but its face, and also by what would power the block
 *   above it, without being told when that changes: quasi-connectivity;</li>
 *   <li>a piston does not move when it is updated, but on a
 *   {@linkplain RedstoneWorld#blockEvent block event}, later in the same game tick, and what it
 *   moves takes two game ticks to arrive;</li>
 *   <li>a sticky piston retracting while its block is still being pushed lets go of it, which is how
 *   a one tick pulse leaves the block behind.</li>
 * </ul>
 */
public final class PistonBaseBlock implements RedstoneBehaviour {

    /**
     * The piston that only pushes.
     */
    public static final PistonBaseBlock NORMAL = new PistonBaseBlock(BlockTypeKeys.PISTON, false);

    /**
     * The piston that pulls its block back.
     */
    public static final PistonBaseBlock STICKY = new PistonBaseBlock(BlockTypeKeys.STICKY_PISTON, true);

    /**
     * The block event extending a piston.
     */
    static final int EVENT_EXTEND = 0;

    /**
     * The block event retracting a piston, a sticky one pulling its block back.
     */
    static final int EVENT_RETRACT = 1;

    /**
     * The block event retracting a sticky piston that lets go of its block.
     */
    static final int EVENT_DROP = 2;

    private final TypedKey<BlockType> key;
    private final boolean sticky;

    private PistonBaseBlock(final TypedKey<BlockType> key, final boolean sticky) {
        this.key = key;
        this.sticky = sticky;
    }

    /**
     * {@return {@code true} if a piston is powered}: through any side but its face, or by what
     * would power the block above it, vanilla's quasi-connectivity
     */
    private static boolean isPowered(final RedstoneWorld world, final BlockPosition position, final BlockFace facing) {
        for (final BlockFace side : Faces.ALL) {
            if (side != facing && world.hasSignal(position.relative(side), side)) {
                return true;
            }
        }
        if (world.hasSignal(position, BlockFace.DOWN)) {
            return true;
        }
        final BlockPosition above = position.relative(BlockFace.UP);
        for (final BlockFace side : Faces.ALL) {
            if (side != BlockFace.DOWN && world.hasSignal(above.relative(side), side)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMovingPiston(final @Nullable BlockData state) {
        return state != null && state.key().equals(BlockTypeKeys.MOVING_PISTON);
    }

    private static void updateShapesAround(final RedstoneWorld world, final BlockPosition position) {
        for (final BlockFace face : Faces.SHAPE_ORDER) {
            world.neighborShapeChanged(position.relative(face), face.opposite());
        }
    }

    private static void updateIndirectShapes(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        final RedstoneBehaviour behaviour = Redstone.behaviour(state);
        if (behaviour != null) {
            behaviour.updateIndirectNeighborShapes(world, position, state);
        }
    }

    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        if (!state.key().equals(previous.key())) {
            checkIfExtend(world, position, state);
        }
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        checkIfExtend(world, position, state);
    }

    /**
     * Queues the block event that brings the piston in line with its power, when it is not.
     */
    private void checkIfExtend(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Piston piston)) {
            return;
        }
        final BlockFace facing = piston.getFacing();
        final boolean powered = isPowered(world, position, facing);
        if (powered && !piston.isExtended()) {
            if (new PistonStructureResolver(world, position, facing, true).resolve()) {
                world.blockEvent(position, key, EVENT_EXTEND, facing.ordinal());
            }
        } else if (!powered && piston.isExtended()) {
            final MovingPistons.MovingBlock pushed = pushedBlock(world, position.relative(facing, 2), facing);
            final boolean letGo = pushed != null && Redstone.movingPistons().isStartingToMove(world.world(), pushed);
            world.blockEvent(position, key, letGo ? EVENT_DROP : EVENT_RETRACT, facing.ordinal());
        }
    }

    /**
     * {@return the block this piston is still pushing to a position, {@code null} when it is not
     * pushing one there}
     */
    private static MovingPistons.@Nullable MovingBlock pushedBlock(final RedstoneWorld world, final BlockPosition position,
                                                                   final BlockFace facing) {
        final BlockData state = world.blockAt(position);
        if (!isMovingPiston(state) || !(state instanceof final Directional directional) || directional.getFacing() != facing) {
            return null;
        }
        final MovingPistons.MovingBlock moving = Redstone.movingPistons().at(world.world(), position);
        return moving != null && moving.facing() == facing && moving.extending() ? moving : null;
    }

    @Override
    public boolean triggerEvent(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final int type, final int data) {
        if (!(state instanceof final Piston piston) || !(world.world() instanceof final ServerWorld serverWorld)) {
            return false;
        }
        final BlockFace facing = piston.getFacing();
        final BlockPosition headPosition = position.relative(facing);
        if (!Redstone.owns(world, position) || !Redstone.owns(world, headPosition)) {
            return false; // a piston never moves across the border of its region
        }
        final Piston extended = piston.withExtended(true);
        final boolean powered = isPowered(world, position, facing);
        if (powered && (type == EVENT_RETRACT || type == EVENT_DROP)) {
            // powered again before it retracted: it stays out
            world.setBlock(position, extended, RedstoneWorld.Update.SHAPES);
            return false;
        }
        if (!powered && type == EVENT_EXTEND) {
            return false;
        }
        if (type == EVENT_EXTEND) {
            if (!moveBlocks(world, serverWorld, position, piston, facing, true)) {
                return false;
            }
            world.setBlock(position, extended, RedstoneWorld.Update.NEIGHBORS);
            Redstone.playSound(serverWorld, position, SoundEventKeys.BLOCK_PISTON_EXTEND, Sound.Source.BLOCK, 0.5f,
                    ThreadLocalRandom.current().nextFloat() * 0.25f + 0.6f, null);
            Redstone.gameEvent(serverWorld, GameEventKeys.BLOCK_ACTIVATE, position);
            return true;
        }
        if (type == EVENT_RETRACT || type == EVENT_DROP) {
            return retract(world, serverWorld, position, piston, facing, type, data);
        }
        return true;
    }

    /**
     * Retracts the piston: its base becomes a moving block for the two game ticks its head takes to
     * come back in, and a sticky piston pulls its block back, or lets go of it.
     */
    private boolean retract(final RedstoneWorld world, final ServerWorld serverWorld, final BlockPosition position,
                            final Piston piston, final BlockFace facing, final int type, final int data) {
        final MovingPistons moving = Redstone.movingPistons();
        final BlockPosition headPosition = position.relative(facing);
        final BlockPosition front = position.relative(facing, 2);
        if (!fireRetract(world, position, piston, facing, type, headPosition, front)) {
            return false;
        }
        // a head still coming out disappears
        moving.finish(world, headPosition);

        final BlockData movingBase = ((TechnicalPiston) Redstone.defaultState(BlockTypeKeys.MOVING_PISTON))
                .withSticky(sticky).withFacing(facing);
        Redstone.setBlockUnseen(world, position, movingBase, RedstoneWorld.Update.QUIET);
        final BlockData retracted = ((Piston) Redstone.defaultState(key)).withFacing(BlockFace.byId(data & 7));
        moving.start(serverWorld, position, retracted, facing, false, true);
        world.updateNeighbors(position, BlockTypeKeys.MOVING_PISTON);
        updateShapesAround(world, position);

        if (sticky) {
            if (pushedBlock(world, front, facing) != null) {
                // the block is still on its way out: it lands where it was going, and stays there
                moving.finish(world, front);
            } else if (type == EVENT_RETRACT && pulls(world, front, facing)) {
                moveBlocks(world, serverWorld, position, piston, facing, false);
            } else {
                removeHead(world, headPosition);
            }
        } else {
            removeHead(world, headPosition);
        }
        Redstone.playSound(serverWorld, position, SoundEventKeys.BLOCK_PISTON_CONTRACT, Sound.Source.BLOCK, 0.5f,
                ThreadLocalRandom.current().nextFloat() * 0.15f + 0.6f, null);
        Redstone.gameEvent(serverWorld, GameEventKeys.BLOCK_DEACTIVATE, position);
        return true;
    }

    /**
     * {@return {@code true} if a sticky piston pulls the block in front of its head back}
     */
    private static boolean pulls(final RedstoneWorld world, final BlockPosition front, final BlockFace facing) {
        final BlockData state = Redstone.owns(world, front) ? world.blockAt(front) : null;
        if (state == null || state.isAir()) {
            return false;
        }
        if (!PistonPushReaction.isPushable(world.world(), state, front, facing.opposite(), false, facing)) {
            return false;
        }
        return PistonPushReaction.of(state) == PistonPushReaction.Reaction.NORMAL || PistonPushReaction.isPistonBase(state);
    }

    private static void removeHead(final RedstoneWorld world, final BlockPosition headPosition) {
        final BlockData head = world.blockAt(headPosition);
        if (head != null && !head.isAir()) {
            world.setBlock(headPosition, Redstone.air(), RedstoneWorld.Update.NEIGHBORS);
        }
    }

    /**
     * Fires the retract event of the plugins, with what a sticky piston is about to pull worked out
     * ahead of the retraction when a plugin listens.
     */
    private boolean fireRetract(final RedstoneWorld world, final BlockPosition position, final Piston piston, final BlockFace facing,
                                final int type, final BlockPosition headPosition, final BlockPosition front) {
        if (!RedstoneEvents.listensToPistonRetract()) {
            return true;
        }
        List<BlockPosition> moved = List.of();
        List<BlockPosition> broken = List.of();
        if (sticky && type == EVENT_RETRACT && pushedBlock(world, front, facing) == null && pulls(world, front, facing)) {
            final PistonStructureResolver resolver = new PistonStructureResolver(world, position, facing, false, headPosition);
            if (resolver.resolve()) {
                moved = resolver.toPush();
                broken = resolver.toDestroy();
            }
        }
        return RedstoneEvents.pistonRetract(world.world(), position, piston, facing, sticky, moved, broken);
    }

    /**
     * Moves what the piston pushes or pulls: every block turns into a moving block one block
     * further, which lands two game ticks later, and the blocks in the way that do not withstand it
     * break. All of it in vanilla's order.
     *
     * @return {@code false} if something holds the piston back, or a plugin cancelled the move
     */
    private boolean moveBlocks(final RedstoneWorld world, final ServerWorld serverWorld, final BlockPosition position,
                               final Piston piston, final BlockFace facing, final boolean extending) {
        final MovingPistons moving = Redstone.movingPistons();
        final BlockPosition headPosition = position.relative(facing);
        if (!extending) {
            final BlockData head = world.blockAt(headPosition);
            if (head != null && head.key().equals(BlockTypeKeys.PISTON_HEAD)) {
                // out of the way of what is pulled; no update, the base already moved on
                Redstone.setBlockUnseen(world, headPosition, Redstone.air(), RedstoneWorld.Update.QUIET);
            }
        }
        final PistonStructureResolver resolver = new PistonStructureResolver(world, position, facing, extending);
        if (!resolver.resolve()) {
            return false;
        }
        final List<BlockPosition> toPush = resolver.toPush();
        final List<BlockPosition> toDestroy = resolver.toDestroy();
        if (extending && !RedstoneEvents.pistonExtend(world.world(), position, piston, facing, sticky, toPush, toDestroy)) {
            return false;
        }

        // where the pushed blocks are, for those of them nothing moves into
        final Map<BlockPosition, BlockData> vacated = new LinkedHashMap<>();
        final List<BlockData> pushedStates = new ArrayList<>(toPush.size());
        for (final BlockPosition pushed : toPush) {
            final BlockData state = world.blockAt(pushed);
            if (state == null) {
                return false;
            }
            pushedStates.add(state);
            vacated.put(pushed, state);
        }
        final BlockFace movement = extending ? facing : facing.opposite();
        final List<BlockData> affected = new ArrayList<>(toPush.size() + toDestroy.size());
        final BlockData air = Redstone.air();

        // the blocks in the way break, furthest first; the server has no block drops to give yet
        for (int index = toDestroy.size() - 1; index >= 0; index--) {
            final BlockPosition destroyed = toDestroy.get(index);
            final BlockData state = world.blockAt(destroyed);
            world.setBlock(destroyed, air, RedstoneWorld.Update.QUIET);
            Redstone.gameEvent(serverWorld, GameEventKeys.BLOCK_DESTROY, destroyed);
            affected.add(state == null ? air : state);
        }

        // then the pushed blocks move, furthest first, each into the place the next one left
        final BlockData movingBlock = ((TechnicalPiston) Redstone.defaultState(BlockTypeKeys.MOVING_PISTON)).withFacing(facing);
        for (int index = toPush.size() - 1; index >= 0; index--) {
            final BlockPosition source = toPush.get(index);
            final BlockData state = world.blockAt(source);
            final BlockPosition destination = source.relative(movement);
            vacated.remove(destination);
            Redstone.setBlockUnseen(world, destination, movingBlock, RedstoneWorld.Update.SHAPES);
            moving.start(serverWorld, destination, pushedStates.get(index), facing, extending, false);
            affected.add(state == null ? air : state);
        }

        if (extending) {
            final BlockData head = ((PistonHead) Redstone.defaultState(BlockTypeKeys.PISTON_HEAD)).withSticky(sticky).withFacing(facing);
            final BlockData movingHead = ((TechnicalPiston) Redstone.defaultState(BlockTypeKeys.MOVING_PISTON))
                    .withSticky(sticky).withFacing(facing);
            vacated.remove(headPosition);
            Redstone.setBlockUnseen(world, headPosition, movingHead, RedstoneWorld.Update.SHAPES);
            moving.start(serverWorld, headPosition, head, facing, true, true);
        }

        // what was left behind is air
        for (final BlockPosition left : vacated.keySet()) {
            world.setBlock(left, air, RedstoneWorld.Update.QUIET);
        }
        for (final Map.Entry<BlockPosition, BlockData> left : vacated.entrySet()) {
            updateIndirectShapes(world, left.getKey(), left.getValue());
            updateShapesAround(world, left.getKey());
        }

        // and the neighbours hear of it all, in the order the blocks were handled
        int handled = 0;
        for (int index = toDestroy.size() - 1; index >= 0; index--) {
            final BlockData state = affected.get(handled++);
            final BlockPosition destroyed = toDestroy.get(index);
            final RedstoneBehaviour behaviour = Redstone.behaviour(state);
            if (behaviour != null) {
                behaviour.onRemoved(world, destroyed, state);
                behaviour.updateIndirectNeighborShapes(world, destroyed, state);
            }
            world.updateNeighbors(destroyed, state.key());
        }
        for (int index = toPush.size() - 1; index >= 0; index--) {
            world.updateNeighbors(toPush.get(index), affected.get(handled++).key());
        }
        if (extending) {
            world.updateNeighbors(headPosition, BlockTypeKeys.PISTON_HEAD);
        }
        return true;
    }
}
