package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.redstone.RedstoneWorld;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Works out what a piston moves: the blocks in front of its head, those slime and honey blocks
 * drag along, and the blocks in the way that break. It follows vanilla block for block, down to the
 * order the blocks are listed in, which is the order they are then moved and updated in.
 *
 * <p>A piston moves at most {@value #MAX_PUSHED} blocks: one more, or a block that does not move
 * in the way, and it does not move at all.</p>
 *
 * <p>A block that is not loaded, or that the calling thread does not own, counts as one that does
 * not move: a piston never moves a block across the border of its region.</p>
 */
final class PistonStructureResolver {

    /**
     * How many blocks a piston moves at most.
     */
    static final int MAX_PUSHED = 12;

    private final RedstoneWorld world;
    private final BlockPosition piston;
    private final BlockFace pistonFacing;
    private final boolean extending;
    private final BlockPosition start;
    private final BlockFace movement;
    private final @Nullable BlockPosition assumedAir;
    private final List<BlockPosition> toPush = new ArrayList<>();
    private final List<BlockPosition> toDestroy = new ArrayList<>();

    /**
     * @param world        the redstone of the world
     * @param piston       where the base of the piston is
     * @param pistonFacing the side the piston faces
     * @param extending    {@code true} to work out what the piston pushes, {@code false} for what it
     *                     pulls
     */
    PistonStructureResolver(final RedstoneWorld world, final BlockPosition piston, final BlockFace pistonFacing, final boolean extending) {
        this(world, piston, pistonFacing, extending, null);
    }

    /**
     * @param world        the redstone of the world
     * @param piston       where the base of the piston is
     * @param pistonFacing the side the piston faces
     * @param extending    {@code true} to work out what the piston pushes, {@code false} for what it
     *                     pulls
     * @param assumedAir   a block read as air, {@code null} for none: the head of a piston whose
     *                     retraction is worked out before the head is taken away
     */
    PistonStructureResolver(final RedstoneWorld world, final BlockPosition piston, final BlockFace pistonFacing, final boolean extending,
                            final @Nullable BlockPosition assumedAir) {
        this.world = world;
        this.piston = piston;
        this.pistonFacing = pistonFacing;
        this.extending = extending;
        this.assumedAir = assumedAir;
        if (extending) {
            this.movement = pistonFacing;
            this.start = piston.relative(pistonFacing);
        } else {
            this.movement = pistonFacing.opposite();
            this.start = piston.relative(pistonFacing, 2);
        }
    }

    private static boolean isSticky(final @Nullable BlockData state) {
        return state != null && (state.key().equals(BlockTypeKeys.SLIME_BLOCK) || state.key().equals(BlockTypeKeys.HONEY_BLOCK));
    }

    /**
     * {@return {@code true} if two blocks next to each other move together}: one of them is slime or
     * honey, and they are not one of each
     */
    private static boolean stickTogether(final BlockData first, final BlockData second) {
        final boolean firstHoney = first.key().equals(BlockTypeKeys.HONEY_BLOCK);
        final boolean firstSlime = first.key().equals(BlockTypeKeys.SLIME_BLOCK);
        final boolean secondHoney = second.key().equals(BlockTypeKeys.HONEY_BLOCK);
        final boolean secondSlime = second.key().equals(BlockTypeKeys.SLIME_BLOCK);
        if ((firstHoney && secondSlime) || (firstSlime && secondHoney)) {
            return false;
        }
        return firstHoney || firstSlime || secondHoney || secondSlime;
    }

    /**
     * Works out what the piston moves.
     *
     * @return {@code true} if the piston can move, {@link #toPush()} and {@link #toDestroy()} then
     *         holding what it moves and breaks; {@code false} if something holds it back
     */
    boolean resolve() {
        toPush.clear();
        toDestroy.clear();
        final BlockData first = stateAt(start);
        if (!isPushable(first, start, false, pistonFacing)) {
            if (extending && first != null && PistonPushReaction.of(first) == PistonPushReaction.Reaction.DESTROY) {
                toDestroy.add(start);
                return true;
            }
            return false;
        }
        if (!addLine(start, movement)) {
            return false;
        }
        // the list grows while it is walked: what a sticky block drags may be sticky too
        for (int index = 0; index < toPush.size(); index++) {
            final BlockPosition pushed = toPush.get(index);
            if (isSticky(stateAt(pushed)) && !addBranches(pushed)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a line of blocks moving together: the block at {@code origin}, the sticky blocks behind
     * it, and the blocks in front of it up to the first gap.
     *
     * @param origin where the line is entered
     * @param side   the side {@code origin} is reached from
     * @return {@code false} if the line cannot move, and the piston with it
     */
    private boolean addLine(final BlockPosition origin, final BlockFace side) {
        BlockData state = stateAt(origin);
        if (state == null || state.isAir() || !isPushable(state, origin, false, side)
                || origin.equals(piston) || toPush.contains(origin)) {
            return true; // nothing to drag along there
        }
        int length = 1;
        if (length + toPush.size() > MAX_PUSHED) {
            return false;
        }
        final BlockFace backwards = movement.opposite();
        // a sticky block drags the blocks behind it
        while (isSticky(state)) {
            final BlockPosition behind = origin.relative(backwards, length);
            final BlockData dragging = state;
            state = stateAt(behind);
            if (state == null || state.isAir() || !stickTogether(dragging, state)
                    || !isPushable(state, behind, false, backwards) || behind.equals(piston)) {
                break;
            }
            length++;
            if (length + toPush.size() > MAX_PUSHED) {
                return false;
            }
        }
        int added = 0;
        for (int back = length - 1; back >= 0; back--) {
            toPush.add(origin.relative(backwards, back));
            added++;
        }
        // then the blocks in front of it, until one has room to move
        for (int ahead = 1; ; ahead++) {
            final BlockPosition next = origin.relative(movement, ahead);
            final int known = toPush.indexOf(next);
            if (known > -1) {
                // the line runs into one already listed: it has to move before that one does
                moveBefore(added, known);
                for (int index = 0; index <= known + added; index++) {
                    final BlockPosition pushed = toPush.get(index);
                    if (isSticky(stateAt(pushed)) && !addBranches(pushed)) {
                        return false;
                    }
                }
                return true;
            }
            state = stateAt(next);
            if (state != null && state.isAir()) {
                return true;
            }
            if (state == null || !isPushable(state, next, true, movement) || next.equals(piston)) {
                return false;
            }
            if (PistonPushReaction.of(state) == PistonPushReaction.Reaction.DESTROY) {
                toDestroy.add(next);
                return true;
            }
            if (toPush.size() >= MAX_PUSHED) {
                return false;
            }
            toPush.add(next);
            added++;
        }
    }

    /**
     * Moves the last {@code count} blocks listed before the one at {@code index}.
     */
    private void moveBefore(final int count, final int index) {
        final List<BlockPosition> head = new ArrayList<>(toPush.subList(0, index));
        final List<BlockPosition> moved = new ArrayList<>(toPush.subList(toPush.size() - count, toPush.size()));
        final List<BlockPosition> tail = new ArrayList<>(toPush.subList(index, toPush.size() - count));
        toPush.clear();
        toPush.addAll(head);
        toPush.addAll(moved);
        toPush.addAll(tail);
    }

    /**
     * Adds what a sticky block drags along on the sides that are not on the axis it moves on.
     */
    private boolean addBranches(final BlockPosition sticky) {
        final BlockData state = stateAt(sticky);
        if (state == null) {
            return true;
        }
        for (final BlockFace side : BlockFace.values()) {
            if (side == movement || side == movement.opposite()) {
                continue;
            }
            final BlockPosition neighbor = sticky.relative(side);
            final BlockData neighborState = stateAt(neighbor);
            if (neighborState != null && stickTogether(neighborState, state) && !addLine(neighbor, side)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPushable(final @Nullable BlockData state, final BlockPosition position, final boolean allowDestroy,
                               final BlockFace facing) {
        return PistonPushReaction.isPushable(world.world(), state, position, movement, allowDestroy, facing);
    }

    private @Nullable BlockData stateAt(final BlockPosition position) {
        if (position.equals(assumedAir)) {
            return Redstone.air();
        }
        return Redstone.owns(world, position) ? world.blockAt(position) : null;
    }

    /**
     * {@return where the blocks the piston moves are, in the order vanilla lists them}
     */
    List<BlockPosition> toPush() {
        return Collections.unmodifiableList(toPush);
    }

    /**
     * {@return where the blocks the piston breaks are, in the order vanilla lists them}
     */
    List<BlockPosition> toDestroy() {
        return Collections.unmodifiableList(toDestroy);
    }
}
