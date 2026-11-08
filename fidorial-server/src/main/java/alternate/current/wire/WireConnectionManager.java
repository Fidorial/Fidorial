/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import alternate.current.wire.WireHandler.Directions;
import alternate.current.wire.WireHandler.NodeProvider;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * The connections of a dust to the dust around it, the way vanilla dust passes power: beside it,
 * and one block up or down where no conductor is in the way.
 *
 * <p>Alternate Current's {@code WireConnectionManager}.</p>
 *
 * @author Space Walker
 */
final class WireConnectionManager {

    private final WireNode owner;

    /**
     * The first connection on each horizontal side; the connections of a side follow each other.
     */
    private final @Nullable WireConnection[] heads = new WireConnection[Directions.HORIZONTAL.length];

    private @Nullable WireConnection head;
    private @Nullable WireConnection tail;

    /**
     * The number of connections.
     */
    int total;

    /**
     * Four bits, one per horizontal side with a connection.
     */
    private int flowTotal;

    /**
     * The direction of flow the connections give, {@code -1} when it is ambiguous.
     */
    int iFlowDir = -1;

    WireConnectionManager(final WireNode owner) {
        this.owner = owner;
    }

    /**
     * Finds the connections of the owner again.
     *
     * @param nodes how to reach the neighbours of a node
     */
    void set(final NodeProvider nodes) {
        if (total > 0) {
            clear();
        }
        final boolean belowIsConductor = nodes.getNeighbor(owner, Directions.DOWN).isConductor();
        final boolean aboveIsConductor = nodes.getNeighbor(owner, Directions.UP).isConductor();
        for (int iDir = 0; iDir < Directions.HORIZONTAL.length; iDir++) {
            final Node neighbor = nodes.getNeighbor(owner, iDir);
            if (neighbor.isWire()) {
                add(neighbor.asWire(), iDir, true, true);
                continue;
            }
            final boolean sideIsConductor = neighbor.isConductor();
            if (!sideIsConductor) {
                final Node below = nodes.getNeighbor(neighbor, Directions.DOWN);
                if (below.isWire()) {
                    add(below.asWire(), iDir, belowIsConductor, true);
                }
            }
            if (!aboveIsConductor) {
                final Node above = nodes.getNeighbor(neighbor, Directions.UP);
                if (above.isWire()) {
                    add(above.asWire(), iDir, true, sideIsConductor);
                }
            }
        }
        if (total > 0) {
            iFlowDir = WireHandler.FLOW_IN_TO_FLOW_OUT[flowTotal];
        }
    }

    private void clear() {
        Arrays.fill(heads, null);
        head = null;
        tail = null;
        total = 0;
        flowTotal = 0;
        iFlowDir = -1;
    }

    private void add(final WireNode wire, final int iDir, final boolean offer, final boolean accept) {
        final WireConnection connection = new WireConnection(wire, iDir, offer, accept);
        final WireConnection last = tail;
        if (last == null) {
            head = connection;
        } else {
            last.next = connection;
        }
        tail = connection;
        total++;
        if (heads[iDir] == null) {
            heads[iDir] = connection;
            flowTotal |= 1 << iDir;
        }
    }

    /**
     * {@return the first connection, to walk them all when their order does not matter}
     */
    @Nullable WireConnection first() {
        return head;
    }

    /**
     * {@return the first connection on a side, to walk them side by side}: the connections of that
     * side are this one and those following it with the same {@link WireConnection#iDir}
     *
     * @param iDir a horizontal {@link Directions} index
     */
    @Nullable WireConnection first(final int iDir) {
        return heads[iDir];
    }
}
