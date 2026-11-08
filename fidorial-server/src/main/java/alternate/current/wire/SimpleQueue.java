/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import org.jspecify.annotations.Nullable;

/**
 * A first in, first out queue of dust, linked through {@link WireNode#nextWire}, for the
 * breadth-first search through a network. Dust can be added while the queue is walked from
 * {@link #head()}, which the search relies on.
 *
 * <p>Alternate Current's {@code SimpleQueue}, without the {@link java.util.Queue} interface the
 * handler does not need.</p>
 *
 * @author Space Walker
 */
final class SimpleQueue {

    private @Nullable WireNode head;
    private @Nullable WireNode tail;

    void offer(final WireNode wire) {
        final WireNode last = tail;
        if (last == null) {
            head = wire;
        } else {
            last.nextWire = wire;
        }
        tail = wire;
    }

    @Nullable WireNode poll() {
        final WireNode first = head;
        if (first == null) {
            return null;
        }
        final WireNode next = first.nextWire;
        first.nextWire = null;
        head = next;
        if (next == null) {
            tail = null;
        }
        return first;
    }

    /**
     * {@return the first dust of the queue, to walk it through {@link WireNode#nextWire}}
     */
    @Nullable WireNode head() {
        return head;
    }

    boolean isEmpty() {
        return head == null;
    }

    void clear() {
        WireNode wire = head;
        while (wire != null) {
            final WireNode next = wire.nextWire;
            wire.nextWire = null;
            wire = next;
        }
        head = null;
        tail = null;
    }
}
