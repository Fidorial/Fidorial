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
    private volatile Listener listener = (_, _, _, _) -> {
    };

    /**
     * Sets who to tell when the number of players with a block's window open changes, like
     * vanilla's {@code ContainerOpenersCounter.openerCountChanged}: a trapped chest powers redstone
     * with it.
     *
     * @param listener told of every change, on the thread opening or closing the window
     */
    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    /**
     * Records a player opening the block's window.
     *
     * @return how many players now have it open
     */
    public int open(final Key world, final BlockPosition position) {
        final int viewers = viewed.computeIfAbsent(new At(world, position), _ -> new Viewed()).open();
        listener.viewersChanged(world, position, viewers - 1, viewers);
        return viewers;
    }

    /**
     * Records a player closing the block's window. The shared contents are dropped with the last viewer.
     *
     * @return how many players still have it open
     */
    public int close(final Key world, final BlockPosition position) {
        final int[] viewers = {0, 0};
        viewed.computeIfPresent(new At(world, position), (_, entry) -> {
            viewers[0] = entry.viewers();
            viewers[1] = entry.close();
            return viewers[1] == 0 ? null : entry;
        });
        if (viewers[0] != viewers[1]) {
            listener.viewersChanged(world, position, viewers[0], viewers[1]);
        }
        return viewers[1];
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
            if (entry.shared == null || entry.viewers() == 0) {
                entry.shared = loader.get();
            }
            return (T) entry.shared;
        }
    }

    public int count(final Key world, final BlockPosition position) {
        final Viewed entry = viewed.get(new At(world, position));
        return entry == null ? 0 : entry.viewers();
    }

    /**
     * Told when the number of players with a block's window open changes.
     */
    @FunctionalInterface
    public interface Listener {
        void viewersChanged(Key world, BlockPosition position, int previous, int current);
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

        synchronized int viewers() {
            return viewers;
        }
    }
}
