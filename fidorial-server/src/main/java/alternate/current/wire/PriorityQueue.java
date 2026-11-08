/*
 * Derived from Alternate Current (https://github.com/SpaceWalkerRS/alternate-current).
 * Copyright (c) 2022 Space Walker. Licensed under the MIT License: see licenses/ALTERNATE-CURRENT.md
 * at the root of the repository, or META-INF/licenses/ALTERNATE-CURRENT.txt in the server jar.
 */
package alternate.current.wire;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * The power changes and block updates waiting to run, linked through {@link Node#prevNode} and
 * {@link Node#nextNode}: highest {@linkplain Node#priority() priority} first, so that power spreads
 * from the strongest dust down, and first in, first out within a priority.
 *
 * <p>A node is queued at most once: queuing it again with another priority moves it.</p>
 *
 * <p>Alternate Current's {@code PriorityQueue}, without the {@link java.util.Queue} interface the
 * handler does not need.</p>
 *
 * @author Space Walker
 */
final class PriorityQueue {

    private static final int OFFSET = -WireHandler.POWER_MIN;

    /**
     * The last node queued with each priority.
     */
    private final @Nullable Node[] tails = new Node[WireHandler.POWER_MAX + OFFSET + 1];

    private @Nullable Node head;
    private @Nullable Node tail;

    /**
     * Queues a node.
     *
     * @return {@code false} if it was already queued with that priority
     */
    boolean offer(final Node node) {
        final int priority = node.priority();
        if (contains(node)) {
            if (node.priority == priority) {
                return false;
            }
            remove(node);
        }
        insert(node, priority);
        return true;
    }

    @Nullable Node poll() {
        final Node first = head;
        if (first == null) {
            return null;
        }
        final Node next = first.nextNode;
        if (next == null) {
            clear();
            return first;
        }
        if (first.priority != next.priority) {
            // the head was also the last node of its priority
            tails[first.priority + OFFSET] = null;
        }
        first.nextNode = null;
        next.prevNode = null;
        head = next;
        return first;
    }

    boolean isEmpty() {
        return head == null;
    }

    void clear() {
        Node node = head;
        while (node != null) {
            final Node next = node.nextNode;
            node.prevNode = null;
            node.nextNode = null;
            node = next;
        }
        Arrays.fill(tails, null);
        head = null;
        tail = null;
    }

    private boolean contains(final Node node) {
        return node == head || node.prevNode != null;
    }

    private void remove(final Node node) {
        final Node prev = node.prevNode;
        final Node next = node.nextNode;
        if (next == null || node.priority != next.priority) {
            // the node was the last of its priority: the one before it takes its place, if any
            tails[node.priority + OFFSET] = prev == null || node.priority != prev.priority ? null : prev;
        }
        if (prev == null) {
            head = next;
        } else {
            prev.nextNode = next;
        }
        if (next == null) {
            tail = prev;
        } else {
            next.prevNode = prev;
        }
        node.prevNode = null;
        node.nextNode = null;
    }

    private void insert(final Node node, final int priority) {
        node.priority = priority;
        final Node first = head;
        final Node last = tail;
        if (first == null || last == null) {
            head = node;
            tail = node;
        } else if (priority > first.priority) {
            node.nextNode = first;
            first.prevNode = node;
            head = node;
        } else if (priority <= last.priority) {
            last.nextNode = node;
            node.prevNode = last;
            tail = node;
        } else {
            // neither the head nor the tail: a node of a higher priority comes before it
            final Node prev = findPrev(priority);
            final Node next = prev.nextNode;
            prev.nextNode = node;
            node.prevNode = prev;
            node.nextNode = next;
            if (next != null) {
                next.prevNode = node;
            }
        }
        tails[priority + OFFSET] = node;
    }

    /**
     * {@return the last node of the lowest priority at or above {@code priority}}, after which a node
     * of that priority goes
     */
    private Node findPrev(final int priority) {
        for (int index = priority + OFFSET; index < tails.length; index++) {
            final Node prev = tails[index];
            if (prev != null) {
                return prev;
            }
        }
        throw new IllegalStateException("No node to insert after for priority " + priority);
    }
}
