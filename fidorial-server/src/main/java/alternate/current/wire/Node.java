/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import alternate.current.wire.WireHandler.Directions;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * A block around a network of redstone dust, as {@link WireHandler} sees it: its state, read once,
 * and links to its neighbours, so that walking the network does not read the world again.
 *
 * <p>Alternate Current's {@code Node}: a node is also used for a block that is not loaded, with no
 * state, which nothing powers and nothing updates.</p>
 *
 * @author Space Walker
 */
class Node {

    private static final int CONDUCTOR = 0b01;
    private static final int SOURCE = 0b10;

    /**
     * The neighbours already looked up, by {@link Directions} index.
     */
    final @Nullable Node[] neighbors = new Node[Directions.ALL.length];

    BlockPosition pos;
    /**
     * The state of the block, {@code null} when its chunk is not loaded.
     */
    @Nullable BlockData state;
    @Nullable RedstoneBehaviour behaviour;
    /**
     * Set while the network is updated, once the blocks may have changed since the node was read.
     */
    boolean invalid;

    private int flags;

    /**
     * The previous node in the {@link PriorityQueue}.
     */
    @Nullable Node prevNode;
    /**
     * The next node in the {@link PriorityQueue}.
     */
    @Nullable Node nextNode;
    /**
     * The priority the node was queued with.
     */
    int priority;
    /**
     * The dust that queued a block update to this node.
     */
    @Nullable WireNode neighborWire;

    Node(final BlockPosition pos) {
        this.pos = pos;
    }

    /**
     * Points the node at another block, or reads its block again.
     *
     * @param clearNeighbors {@code true} when the node moves to another position
     */
    final Node set(final BlockPosition pos, final @Nullable BlockData state, final @Nullable RedstoneBehaviour behaviour,
                   final boolean clearNeighbors) {
        if (clearNeighbors) {
            Arrays.fill(neighbors, null);
        }
        this.pos = pos;
        this.state = state;
        this.behaviour = behaviour;
        this.invalid = false;
        this.flags = 0;
        if (state != null) {
            if (BlockSupport.isConductor(state)) {
                this.flags |= CONDUCTOR;
            }
            if (behaviour != null && behaviour.isSignalSource(state)) {
                this.flags |= SOURCE;
            }
        }
        return this;
    }

    /**
     * {@return the priority to queue this node with: that of the dust updating it}
     */
    int priority() {
        final WireNode wire = neighborWire;
        return wire == null ? 0 : wire.priority;
    }

    boolean isWire() {
        return false;
    }

    final boolean isConductor() {
        return (flags & CONDUCTOR) != 0;
    }

    final boolean isSignalSource() {
        return (flags & SOURCE) != 0;
    }

    /**
     * {@return {@code true} if a block update to this node can make a difference}: its chunk is
     * loaded and the block takes part in redstone. Air and stone ignore block updates.
     */
    boolean reactsToUpdates() {
        return state != null && behaviour != null && !state.isAir();
    }

    WireNode asWire() {
        throw new UnsupportedOperationException("Not a WireNode: " + pos);
    }
}
