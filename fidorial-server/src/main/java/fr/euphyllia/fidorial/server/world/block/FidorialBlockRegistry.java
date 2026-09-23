package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.plugin.BuiltInPlugin;
import fr.euphyllia.fidorial.server.plugin.PluginOwnedMap;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class FidorialBlockRegistry implements BlockRegistry {

    private final Map<Key, BlockType> types = new ConcurrentHashMap<>();
    private final PluginOwnedMap<Key, BlockBehaviour> behaviours = new PluginOwnedMap<>();
    private final PluginOwnedMap<Key, Key> blockItems = new PluginOwnedMap<>();
    private final Map<Key, BlockBehaviour> fallbackBehaviours = new ConcurrentHashMap<>();
    private final Map<Integer, BlockData> byNetworkId = new ConcurrentHashMap<>();

    @Override
    public void register(final BlockType type) {
        final BlockType previous = types.put(type.key(), type);
        if (previous != null) {
            throw new IllegalStateException("Block '" + type.key().asString() + "' is already registered");
        }
        for (int ordinal = 0; ordinal < type.stateCount(); ordinal++) {
            final BlockData data = type.stateAt(ordinal);
            byNetworkId.putIfAbsent(data.networkId(), data);
        }
    }

    @Override
    public void register(final BlockBehaviour behaviour) {
        register(behaviour, BuiltInPlugin.INSTANCE);
    }

    @Override
    public void register(final BlockBehaviour behaviour, final Plugin owner) {
        if (!types.containsKey(behaviour.key())) {
            register(behaviour.type());
        }
        behaviours.put(behaviour.key(), behaviour, owner);
    }

    @Override
    public void registerBlockItem(final Key item, final Key block, final Plugin owner) {
        blockItems.put(item, block, owner);
    }

    @Override
    public Optional<Key> blockForItem(final Key item) {
        return Optional.ofNullable(blockItems.get(item));
    }

    @Override
    public void unregisterAll(final Plugin owner) {
        behaviours.removeAll(owner);
        blockItems.removeAll(owner);
    }

    public @Nullable BlockBehaviour explicitBehaviour(final Key key) {
        return behaviours.get(key);
    }

    @Override
    public Optional<BlockType> type(final Key key) {
        return Optional.ofNullable(types.get(key));
    }

    @Override
    public @Nullable BlockData fromNetworkId(final int networkId) {
        return byNetworkId.get(networkId);
    }

    // TODO: do sth about this - prismarine data doesn't take into account things like respawn_anchor charges changing the light emission
    // we need to manually register behaviours for such cases for now, we'll need to register custom behaviours for blocks anyway in their respective classes
    @Override
    public Optional<BlockBehaviour> behaviour(final Key key) {
        final BlockBehaviour explicit = behaviours.get(key);
        if (explicit != null) {
            return Optional.of(explicit);
        }
        if (!types.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(fallbackBehaviours.computeIfAbsent(key,
                k -> SimpleBlock.of(k, BlockStateLightProperties.opacity(k), BlockStateLightProperties.emission(k))));
    }

    @Override
    public Collection<BlockType> types() {
        return Collections.unmodifiableCollection(types.values());
    }

    public int definedCount() {
        return types.size();
    }
}
