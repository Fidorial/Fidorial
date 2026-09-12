package fr.euphyllia.fidorial.server.world.block.interaction;

import fr.euphyllia.fidorial.server.world.block.EnderChestBlock;
import fr.euphyllia.fidorial.server.world.block.FarmlandBlock;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.BlockInteractionHandler;
import fr.fidorial.world.block.interaction.BlockInteractionRegistry;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FidorialBlockInteractions implements BlockInteractionRegistry {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialBlockInteractions.class);

    public static final Object BUILT_IN = new Object();

    private final Map<Key, List<Entry>> handlers = new ConcurrentHashMap<>();

    public static FidorialBlockInteractions createDefault() {
        final FidorialBlockInteractions interactions = new FidorialBlockInteractions();
        interactions.register(EnderChestBlock.KEY, EnderChestBlock::open, BUILT_IN);
        interactions.registerAll(FarmlandBlock.tillableBlocks(), FarmlandBlock::till, BUILT_IN);
        return interactions;
    }

    @Override
    public void register(final Key blockType, final BlockInteractionHandler handler, final Object owner) {
        handlers.computeIfAbsent(blockType, _ -> new CopyOnWriteArrayList<>()).add(new Entry(handler, owner));
    }

    @Override
    public boolean unregister(final Key blockType, final Object owner) {
        final List<Entry> entries = handlers.get(blockType);
        if (entries == null) {
            return false;
        }
        final boolean removed = entries.removeIf(entry -> entry.owner() == owner);
        if (entries.isEmpty()) {
            handlers.remove(blockType, entries);
        }
        return removed;
    }

    @Override
    public void unregisterAll(final Object owner) {
        for (final Key blockType : Set.copyOf(handlers.keySet())) {
            unregister(blockType, owner);
        }
    }

    @Override
    public List<BlockInteractionHandler> handlers(final Key blockType) {
        final List<Entry> entries = handlers.get(blockType);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        final List<BlockInteractionHandler> result = new ArrayList<>(entries.size());
        for (final Entry entry : entries) {
            result.add(entry.handler());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Set<Key> interactiveBlocks() {
        return Set.copyOf(handlers.keySet());
    }

    public InteractionResult use(final BlockInteractionContext context) {
        final Key blockType = blockTypeOf(context);
        final List<Entry> entries = handlers.get(blockType);
        if (entries == null) {
            return InteractionResult.PASS;
        }
        for (final Entry entry : entries) {
            final InteractionResult result;
            try {
                result = entry.handler().use(context);
            } catch (final Throwable t) {
                LOGGER.error("Interaction with {} at {} failed", blockType.asString(), context.pos(), t);
                continue;
            }
            if (result.handled()) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    private static Key blockTypeOf(final BlockInteractionContext context) {
        if (context instanceof final FidorialBlockInteractionContext server) {
            return server.state().name();
        }
        return context.block().key();
    }

    private record Entry(BlockInteractionHandler handler, Object owner) {
    }
}
