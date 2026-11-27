package fr.euphyllia.fidorial.server.world;

import fr.fidorial.math.BlockPosition;
import net.kyori.adventure.key.Key;

import java.util.Objects;

/**
 * The locks of the block entities, one per position, shared by stripes.
 *
 * <p>The slots of a block are read and written from more than the thread owning it: players look
 * into a block from the regions around it, and hoppers reach into their neighbours, each on the
 * thread of its own region. Whoever reads then writes the data of a block entity off that thread
 * holds the lock of its position, and so does the thread owning the block when it hands the block
 * entity over to another block or removes it, so that no write slips in between and is lost with
 * it.</p>
 *
 * <p>Several positions share a lock: whoever takes the locks of several positions takes them in
 * the order of their {@linkplain #index index}, the same on every thread, so that two threads never
 * wait for each other. While holding one, a thread neither waits for another thread nor runs the
 * code of plugins.</p>
 */
public final class BlockEntityLocks {

    private static final Object[] LOCKS = new Object[64];

    static {
        for (int index = 0; index < LOCKS.length; index++) {
            LOCKS[index] = new Object();
        }
    }

    private BlockEntityLocks() {
        throw new UnsupportedOperationException("BlockEntityLocks cannot be instantiated.");
    }

    /**
     * {@return the index of the lock of a position, the order in which locks are taken}
     *
     * @param world    the key of the world
     * @param position the position of the block
     */
    public static int index(final Key world, final BlockPosition position) {
        return Math.floorMod(Objects.hash(world, position), LOCKS.length);
    }

    /**
     * {@return the lock of an index}
     *
     * @param index what {@link #index} gave
     */
    public static Object lock(final int index) {
        return LOCKS[index];
    }

    /**
     * {@return the lock of the block entity at a position}
     *
     * @param world    the world of the block
     * @param position the position of the block
     */
    public static Object of(final ServerWorld world, final BlockPosition position) {
        return LOCKS[index(world.key(), position)];
    }
}
