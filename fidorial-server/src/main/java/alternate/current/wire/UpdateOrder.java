/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import alternate.current.wire.WireHandler.NodeProvider;

import java.util.Locale;

import static alternate.current.wire.WireHandler.Directions.DOWN;
import static alternate.current.wire.WireHandler.Directions.EAST;
import static alternate.current.wire.WireHandler.Directions.NORTH;
import static alternate.current.wire.WireHandler.Directions.SOUTH;
import static alternate.current.wire.WireHandler.Directions.UP;
import static alternate.current.wire.WireHandler.Directions.WEST;

/**
 * The order Alternate Current updates the blocks around a wire in, chosen per world in its
 * configuration.
 *
 * <p>Every order is relative to the direction power flows through the dust rather than absolute,
 * so that a circuit behaves the same whichever way it faces. Each is built from the vanilla order
 * of shape updates, west, east, north, south, down, up, read with west as the direction of flow:
 * front, back, right, left, down, up.</p>
 *
 * <p>Alternate Current's {@code UpdateOrder}.</p>
 *
 * @author Space Walker
 */
public enum UpdateOrder {

    /**
     * The default: the six direct neighbours first, then the twelve diagonal ones, then the six two
     * blocks away, horizontal neighbours before vertical ones in each group.
     */
    HORIZONTAL_FIRST_OUTWARD(false) {
        @Override
        void forEachNeighbor(final NodeProvider nodes, final WireNode source, final int forward, final Visitor visitor) {
            final Around around = new Around(nodes, source, forward);
            around.direct(visitor);
            around.diagonal(visitor);
            around.far(visitor);
        }
    },

    /**
     * {@link #HORIZONTAL_FIRST_OUTWARD} backwards: the neighbours two blocks away first, then the
     * diagonal ones, then the direct ones.
     */
    HORIZONTAL_FIRST_INWARD(false) {
        @Override
        void forEachNeighbor(final NodeProvider nodes, final WireNode source, final int forward, final Visitor visitor) {
            final Around around = new Around(nodes, source, forward);
            around.far(visitor);
            around.diagonal(visitor);
            around.direct(visitor);
        }
    },

    /**
     * As {@link #HORIZONTAL_FIRST_OUTWARD}, with the vertical neighbours before the horizontal ones
     * in each group.
     */
    VERTICAL_FIRST_OUTWARD(true) {
        @Override
        void forEachNeighbor(final NodeProvider nodes, final WireNode source, final int forward, final Visitor visitor) {
            final Around around = new Around(nodes, source, forward);
            around.directVerticalFirst(visitor);
            around.diagonalVerticalFirst(visitor);
            around.farVerticalFirst(visitor);
        }
    },

    /**
     * {@link #VERTICAL_FIRST_OUTWARD} backwards.
     */
    VERTICAL_FIRST_INWARD(true) {
        @Override
        void forEachNeighbor(final NodeProvider nodes, final WireNode source, final int forward, final Visitor visitor) {
            final Around around = new Around(nodes, source, forward);
            around.farVerticalFirst(visitor);
            around.diagonalVerticalFirst(visitor);
            around.directVerticalFirst(visitor);
        }
    };

    /**
     * The six faces, in relative order, with each horizontal direction as forward.
     */
    private static final int[][] HORIZONTAL_FIRST = {
            {WEST, EAST, NORTH, SOUTH, DOWN, UP},
            {NORTH, SOUTH, EAST, WEST, DOWN, UP},
            {EAST, WEST, SOUTH, NORTH, DOWN, UP},
            {SOUTH, NORTH, WEST, EAST, DOWN, UP}
    };

    private static final int[][] VERTICAL_FIRST = {
            {DOWN, UP, WEST, EAST, NORTH, SOUTH},
            {DOWN, UP, NORTH, SOUTH, EAST, WEST},
            {DOWN, UP, EAST, WEST, SOUTH, NORTH},
            {DOWN, UP, SOUTH, NORTH, WEST, EAST}
    };

    /**
     * The four horizontal faces, in relative order, with each of them as forward.
     */
    private static final int[][] CARDINAL = {
            {WEST, EAST, NORTH, SOUTH},
            {NORTH, SOUTH, EAST, WEST},
            {EAST, WEST, SOUTH, NORTH},
            {SOUTH, NORTH, WEST, EAST}
    };

    private final boolean verticalFirst;

    UpdateOrder(final boolean verticalFirst) {
        this.verticalFirst = verticalFirst;
    }

    /**
     * {@return the order with that identifier}
     *
     * @param id an identifier, such as {@code horizontal_first_outward}
     * @throws IllegalArgumentException if no order has that identifier
     */
    public static UpdateOrder byId(final String id) {
        return valueOf(id.toUpperCase(Locale.ENGLISH));
    }

    /**
     * {@return the identifier of the order, such as {@code horizontal_first_outward}}
     */
    public String id() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * {@return the six faces in the order to walk them, {@code forward} being the direction of flow}
     *
     * @param forward a horizontal {@link WireHandler.Directions} index
     */
    int[] directNeighbors(final int forward) {
        return verticalFirst ? VERTICAL_FIRST[forward] : HORIZONTAL_FIRST[forward];
    }

    /**
     * {@return the four horizontal faces in the order to walk them, {@code forward} being the direction
     * of flow}
     *
     * @param forward a horizontal {@link WireHandler.Directions} index
     */
    int[] cardinalNeighbors(final int forward) {
        return CARDINAL[forward];
    }

    /**
     * Visits the blocks around a dust that it updates when its power changes: its direct neighbours,
     * and those up to two blocks away, whose own neighbours are its direct ones.
     *
     * @param nodes   how to reach the neighbours of a node
     * @param source  the dust
     * @param forward the horizontal {@link WireHandler.Directions} index power flows in through the dust
     * @param visitor what to do with each block
     */
    abstract void forEachNeighbor(NodeProvider nodes, WireNode source, int forward, Visitor visitor);

    /**
     * What to do with each block around a dust.
     */
    @FunctionalInterface
    interface Visitor {

        /**
         * @param node   a block around the dust
         * @param source the dust
         */
        void visit(Node node, WireNode source);
    }

    /**
     * The neighbours of a dust, relative to the direction of flow.
     */
    private static final class Around {

        private final NodeProvider nodes;
        private final WireNode source;
        private final int forward;
        private final int rightward;
        private final int backward;
        private final int leftward;
        private final Node front;
        private final Node right;
        private final Node back;
        private final Node left;
        private final Node below;
        private final Node above;

        Around(final NodeProvider nodes, final WireNode source, final int forward) {
            this.nodes = nodes;
            this.source = source;
            this.forward = forward;
            this.rightward = (forward + 1) & 0b11;
            this.backward = (forward + 2) & 0b11;
            this.leftward = (forward + 3) & 0b11;
            this.front = nodes.getNeighbor(source, forward);
            this.right = nodes.getNeighbor(source, rightward);
            this.back = nodes.getNeighbor(source, backward);
            this.left = nodes.getNeighbor(source, leftward);
            this.below = nodes.getNeighbor(source, DOWN);
            this.above = nodes.getNeighbor(source, UP);
        }

        private void visit(final Visitor visitor, final Node node) {
            visitor.visit(node, source);
        }

        private void visit(final Visitor visitor, final Node node, final int iDir) {
            visitor.visit(nodes.getNeighbor(node, iDir), source);
        }

        /**
         * The six direct neighbours: front, back, right, left, below, above.
         */
        void direct(final Visitor visitor) {
            visit(visitor, front);
            visit(visitor, back);
            visit(visitor, right);
            visit(visitor, left);
            visit(visitor, below);
            visit(visitor, above);
        }

        /**
         * The twelve diagonal neighbours, by pairs on opposite sides.
         */
        void diagonal(final Visitor visitor) {
            visit(visitor, front, rightward);
            visit(visitor, back, leftward);
            visit(visitor, front, leftward);
            visit(visitor, back, rightward);
            visit(visitor, front, DOWN);
            visit(visitor, back, UP);
            visit(visitor, front, UP);
            visit(visitor, back, DOWN);
            visit(visitor, right, DOWN);
            visit(visitor, left, UP);
            visit(visitor, right, UP);
            visit(visitor, left, DOWN);
        }

        /**
         * The six neighbours two blocks away.
         */
        void far(final Visitor visitor) {
            visit(visitor, front, forward);
            visit(visitor, back, backward);
            visit(visitor, right, rightward);
            visit(visitor, left, leftward);
            visit(visitor, below, DOWN);
            visit(visitor, above, UP);
        }

        void directVerticalFirst(final Visitor visitor) {
            visit(visitor, below);
            visit(visitor, above);
            visit(visitor, front);
            visit(visitor, back);
            visit(visitor, right);
            visit(visitor, left);
        }

        void diagonalVerticalFirst(final Visitor visitor) {
            visit(visitor, below, forward);
            visit(visitor, above, backward);
            visit(visitor, below, backward);
            visit(visitor, above, forward);
            visit(visitor, below, rightward);
            visit(visitor, above, leftward);
            visit(visitor, below, leftward);
            visit(visitor, above, rightward);
            visit(visitor, front, rightward);
            visit(visitor, back, leftward);
            visit(visitor, front, leftward);
            visit(visitor, back, rightward);
        }

        void farVerticalFirst(final Visitor visitor) {
            visit(visitor, below, DOWN);
            visit(visitor, above, UP);
            visit(visitor, front, forward);
            visit(visitor, back, backward);
            visit(visitor, right, rightward);
            visit(visitor, left, leftward);
        }
    }
}
