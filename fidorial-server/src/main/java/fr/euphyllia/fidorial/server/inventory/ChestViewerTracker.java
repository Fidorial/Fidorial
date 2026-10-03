package fr.euphyllia.fidorial.server.inventory;

import fr.fidorial.math.BlockPosition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjIntConsumer;

public final class ChestViewerTracker {

    private final Map<BlockPosition, Integer> viewers = new ConcurrentHashMap<>();

    /**
     * Records an opening and notifies the callback with the new viewer count.
     */
    public void open(final BlockPosition pos, final ObjIntConsumer<BlockPosition> onChanged) {
        final int count = viewers.merge(pos, 1, Integer::sum);
        onChanged.accept(pos, count);
    }

    /**
     * Records a closing and notifies the callback with the new viewer count.
     */
    public void close(final BlockPosition pos, final ObjIntConsumer<BlockPosition> onChanged) {
        final Integer remaining = viewers.compute(pos, (key, current) -> {
            if (current == null || current <= 1) {
                return null;
            }
            return current - 1;
        });
        onChanged.accept(pos, remaining == null ? 0 : remaining);
    }

    public void forget(final BlockPosition pos) {
        viewers.remove(pos);
    }

    public int count(final BlockPosition pos) {
        return viewers.getOrDefault(pos, 0);
    }
}
