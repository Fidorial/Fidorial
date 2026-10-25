package fr.euphyllia.fidorial.server.inventory;

import fr.fidorial.math.BlockPosition;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Who has a block's window open, like vanilla's {@code ContainerOpenersCounter}: how many players,
 * for the lid of an ender chest, and the contents they all work on while it stays open, for a
 * crafter. Block entities are saved data rather than live objects here, so this is where the
 * contents live between two saves.
 *
 * <p>Blocks are told apart by world, so that the same position in two worlds never mixes up.</p>
 */
public final class BlockViewerTracker {

    private final Map<At, Viewed> viewed = new ConcurrentHashMap<>();

    /**
     * Records a player opening the block's window.
     *
     * @return how many players now have it open
     */
    public int open(final Key world, final BlockPosition position) {
        return viewed.computeIfAbsent(new At(world, position), _ -> new Viewed()).open();
    }

    /**
     * Records a player closing the block's window. The shared contents are dropped with the last viewer.
     *
     * @return how many players still have it open
     */
    public int close(final Key world, final BlockPosition position) {
        final int[] remaining = {0};
        viewed.computeIfPresent(new At(world, position), (_, entry) -> {
            remaining[0] = entry.close();
            return remaining[0] == 0 ? null : entry;
        });
        return remaining[0];
    }

    /**
     * The contents every viewer of the block works on. With nobody looking, they are loaded again
     * by {@code loader}, so that a window that was about to open but did not (a cancelled
     * {@code PlayerOpenInventoryEvent}) never leaves stale contents behind.
     */
    @SuppressWarnings("unchecked")
    public <T> T shared(final Key world, final BlockPosition position, final Supplier<T> loader) {
        final Viewed entry = viewed.computeIfAbsent(new At(world, position), _ -> new Viewed());
        synchronized (entry) {
            if (entry.shared == null || entry.viewers == 0) {
                entry.shared = loader.get();
            }
            return (T) entry.shared;
        }
    }

    public int count(final Key world, final BlockPosition position) {
        final Viewed entry = viewed.get(new At(world, position));
        return entry == null ? 0 : entry.viewers;
    }

    private record At(Key world, BlockPosition position) {
    }

    private static final class Viewed {

        private int viewers;
        private @Nullable Object shared;

        synchronized int open() {
            return ++viewers;
        }

        synchronized int close() {
            viewers = Math.max(0, viewers - 1);
            return viewers;
        }
    }
}
