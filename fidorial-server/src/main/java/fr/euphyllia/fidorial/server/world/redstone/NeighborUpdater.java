package fr.euphyllia.fidorial.server.world.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the block updates of one thread one chain at a time, like vanilla's
 * {@code CollectingNeighborUpdater}: an update caused while another one runs is queued and runs
 * right after it, depth first, instead of nesting inside it. The order is the one recursive calls
 * would give, without the stack overflow a long redstone line would cause.
 *
 * <p>One instance per thread; {@link ServerRedstoneWorld} drops the updates reaching a block another
 * thread owns.</p>
 */
final class NeighborUpdater {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(NeighborUpdater.class);

    private final int maxChainedUpdates;
    private final ArrayDeque<Updates> stack = new ArrayDeque<>();
    private final List<Updates> addedThisLayer = new ArrayList<>();
    private int count;
    private int indirectLimit = FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT;

    /**
     * @param maxChainedUpdates how many updates one chain may cause before the rest is skipped,
     *                          negative for no limit
     */
    NeighborUpdater(final int maxChainedUpdates) {
        this.maxChainedUpdates = maxChainedUpdates;
    }

    /**
     * {@return how deep the shape updates a behaviour asks for while it updates indirect neighbours
     * may still go}
     */
    int indirectLimit() {
        return indirectLimit;
    }

    /**
     * Sets {@link #indirectLimit()}, while a block updates the shapes of its indirect neighbours.
     *
     * @param limit the new limit
     * @return the previous limit, to restore afterwards
     */
    int indirectLimit(final int limit) {
        final int previous = indirectLimit;
        indirectLimit = limit;
        return previous;
    }

    void neighborChanged(final ServerRedstoneWorld world, final BlockPosition position, final TypedKey<BlockType> source) {
        addAndRun(position, new SingleUpdate(world, position, source));
    }

    void updateNeighbors(final ServerRedstoneWorld world, final BlockPosition position, final TypedKey<BlockType> source,
                         final @Nullable BlockFace skipped) {
        addAndRun(position, new MultiUpdate(world, position, source, skipped));
    }

    void shapeUpdate(final ServerRedstoneWorld world, final BlockPosition position, final BlockFace direction,
                     final BlockData neighbor, final int limit) {
        addAndRun(position, new ShapeUpdate(world, position, direction, neighbor, limit));
    }

    private void addAndRun(final BlockPosition position, final Updates update) {
        final boolean runningAlready = count > 0;
        final boolean tooManyUpdates = maxChainedUpdates >= 0 && count >= maxChainedUpdates;
        count++;
        if (!tooManyUpdates) {
            if (runningAlready) {
                addedThisLayer.add(update);
            } else {
                stack.push(update);
            }
        } else if (count - 1 == maxChainedUpdates) {
            LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: {}", position);
        }
        if (!runningAlready) {
            runUpdates();
        }
    }

    private void runUpdates() {
        try {
            while (!stack.isEmpty() || !addedThisLayer.isEmpty()) {
                for (int i = addedThisLayer.size() - 1; i >= 0; i--) {
                    stack.push(addedThisLayer.get(i));
                }
                addedThisLayer.clear();
                final Updates next = stack.peek();
                if (next == null) {
                    break;
                }
                while (addedThisLayer.isEmpty()) {
                    if (!next.runNext()) {
                        stack.pop();
                        break;
                    }
                }
            }
        } finally {
            stack.clear();
            addedThisLayer.clear();
            count = 0;
        }
    }

    /**
     * One or more updates waiting in the stack.
     */
    private interface Updates {

        /**
         * Runs the next update.
         *
         * @return {@code true} if more updates remain
         */
        boolean runNext();
    }

    private record SingleUpdate(ServerRedstoneWorld world, BlockPosition position, TypedKey<BlockType> source)
            implements Updates {
        @Override
        public boolean runNext() {
            world.runNeighborChanged(position, source);
            return false;
        }
    }

    private record ShapeUpdate(ServerRedstoneWorld world, BlockPosition position, BlockFace direction,
                               BlockData neighbor, int limit) implements Updates {
        @Override
        public boolean runNext() {
            world.runShapeUpdate(position, direction, neighbor, limit);
            return false;
        }
    }

    /**
     * The six neighbours of a block, in {@link Faces#UPDATE_ORDER}, one per call.
     */
    private static final class MultiUpdate implements Updates {

        private final ServerRedstoneWorld world;
        private final BlockPosition source;
        private final TypedKey<BlockType> sourceBlock;
        private final @Nullable BlockFace skipped;
        private int index;

        MultiUpdate(final ServerRedstoneWorld world, final BlockPosition source, final TypedKey<BlockType> sourceBlock,
                    final @Nullable BlockFace skipped) {
            this.world = world;
            this.source = source;
            this.sourceBlock = sourceBlock;
            this.skipped = skipped;
            skip();
        }

        @Override
        public boolean runNext() {
            final BlockFace face = Faces.UPDATE_ORDER.get(index++);
            world.runNeighborChanged(source.relative(face), sourceBlock);
            skip();
            return index < Faces.UPDATE_ORDER.size();
        }

        private void skip() {
            if (index < Faces.UPDATE_ORDER.size() && Faces.UPDATE_ORDER.get(index) == skipped) {
                index++;
            }
        }
    }
}
