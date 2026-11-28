package fr.euphyllia.fidorial.server.world.generation.block;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Numbers the block states a generator places, so that chunks in the making can store a {@code char} per block.
 * Numbers are handed out on first use and never change. Safe to share between threads.
 */
public final class StatePalette {

    private static final int MAX_STATES = Character.MAX_VALUE + 1;

    private final ConcurrentHashMap<BlockState, Character> ids = new ConcurrentHashMap<>();
    private volatile BlockState[] states = new BlockState[64];
    private int size;

    /**
     * {@return the number of a state, given one if it has none yet}
     *
     * <p>Lock-free once the state has a number: only the first use of a state takes the lock. Every chunk worker
     * comes through here for each block it places, so a lock on this path makes them all queue behind each
     * other.</p>
     */
    public char id(final BlockState state) {
        final Character known = ids.get(state);
        if (known != null) {
            return known;
        }
        return assign(state);
    }

    private synchronized char assign(final BlockState state) {
        // another thread may have numbered it while this one was waiting for the lock
        final Character again = ids.get(state);
        if (again != null) {
            return again;
        }
        if (size == MAX_STATES) {
            throw new IllegalStateException("Too many block states placed by world generation");
        }
        BlockState[] current = states;
        if (size == current.length) {
            current = Arrays.copyOf(current, current.length * 2);
        }
        current[size] = state;
        // publish the array before the number, so that a reader seeing the number finds the state
        states = current;
        final char id = (char) size++;
        ids.put(state, id);
        return id;
    }

    /**
     * {@return the state of a number handed out before}
     */
    public BlockState state(final char id) {
        return states[id];
    }
}
