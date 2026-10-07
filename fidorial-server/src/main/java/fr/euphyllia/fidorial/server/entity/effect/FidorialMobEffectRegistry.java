package fr.euphyllia.fidorial.server.entity.effect;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.registry.data.FrozenRegistries;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.effect.MobEffectBehaviour;
import fr.fidorial.entity.effect.MobEffectDefinition;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.effect.MobEffectRegistry;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FidorialMobEffectRegistry implements MobEffectRegistry {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialMobEffectRegistry.class);

    private static final Key REGISTRY = Key.key("mob_effect");

    private final Map<Key, Integer> vanillaIds;
    private final Map<Key, Registered> registered = new ConcurrentHashMap<>();
    private final Map<Key, List<Attachment>> attachments = new ConcurrentHashMap<>();

    public FidorialMobEffectRegistry() {
        final List<Key> vanilla = FrozenRegistries.entries().getOrDefault(REGISTRY, List.of());
        final Map<Key, Integer> ids = new HashMap<>(vanilla.size());
        for (int id = 0; id < vanilla.size(); id++) {
            ids.put(vanilla.get(id), id);
        }
        this.vanillaIds = Map.copyOf(ids);
    }

    @Override
    public MobEffectInstance.Builder builder(final TypedKey<MobEffect> type) {
        Preconditions.checkArgument(isEffect(type), "Unknown effect %s", type);
        return new MobEffectInstanceImpl.Builder(type);
    }

    @Override
    public MobEffectDefinition.Builder define(final TypedKey<MobEffect> key) {
        return new MobEffectDefinitionImpl.Builder(key, this::isVanilla);
    }

    @Override
    public synchronized void register(final MobEffectDefinition definition, final Plugin owner) {
        final Key key = definition.key().key();
        Preconditions.checkArgument(!vanillaIds.containsKey(key), "%s is a vanilla effect", key);
        Preconditions.checkArgument(!registered.containsKey(key), "%s is already registered", key);

        final int networkId;
        final OptionalInt requested = definition.requestedNetworkId();
        if (requested.isPresent()) {
            networkId = requested.getAsInt();
            Preconditions.checkArgument(networkId >= vanillaIds.size(),
                    "The network ID %s of %s belongs to a vanilla effect", networkId, key);
            Preconditions.checkArgument(registered.values().stream().noneMatch(r -> r.networkId() == networkId),
                    "The network ID %s of %s is already taken", networkId, key);
        } else {
            networkId = registered.values().stream()
                    .mapToInt(Registered::networkId)
                    .max()
                    .orElse(vanillaIds.size() - 1) + 1;
        }

        registered.put(key, new Registered(definition, owner, networkId));
        LOGGER.info("Effect {} registered by {} (network ID {})", key, ownerName(owner), networkId);
    }

    @Override
    public void attach(final TypedKey<MobEffect> type, final MobEffectBehaviour behaviour, final Plugin owner) {
        Preconditions.checkArgument(behaviour != null, "The behaviour must not be null");
        attachments.computeIfAbsent(type.key(), _ -> new CopyOnWriteArrayList<>())
                .add(new Attachment(behaviour, owner));
        LOGGER.debug("Behaviour attached to {} by {}", type.key(), ownerName(owner));
    }

    @Override
    public synchronized boolean unregister(final TypedKey<MobEffect> type) {
        final Registered removed = registered.remove(type.key());
        if (removed == null) {
            return false;
        }
        LOGGER.info("Effect {} unregistered", type.key());
        return true;
    }

    @Override
    public boolean detach(final TypedKey<MobEffect> type, final Plugin owner) {
        final List<Attachment> list = attachments.get(type.key());
        return list != null && list.removeIf(attachment -> attachment.owner() == owner);
    }

    @Override
    public synchronized void unregisterAll(final Plugin owner) {
        registered.values().removeIf(r -> r.owner() == owner);
        attachments.values().forEach(list -> list.removeIf(attachment -> attachment.owner() == owner));
    }

    @Override
    public Optional<MobEffectDefinition> definition(final TypedKey<MobEffect> type) {
        return Optional.ofNullable(registered.get(type.key())).map(Registered::definition);
    }

    @Override
    public Collection<MobEffectDefinition> definitions() {
        return registered.values().stream().map(Registered::definition).toList();
    }

    @Override
    public Set<TypedKey<MobEffect>> types() {
        final Set<TypedKey<MobEffect>> types = new LinkedHashSet<>();
        vanillaIds.keySet().forEach(key -> types.add(TypedKey.create(RegistryKey.MOB_EFFECT, key)));
        registered.keySet().forEach(key -> types.add(TypedKey.create(RegistryKey.MOB_EFFECT, key)));
        return Set.copyOf(types);
    }

    @Override
    public boolean isEffect(final TypedKey<MobEffect> type) {
        return isKnown(type.key());
    }

    public boolean isKnown(final Key type) {
        return vanillaIds.containsKey(type) || registered.containsKey(type);
    }

    @Override
    public OptionalInt networkId(final TypedKey<MobEffect> type) {
        final Integer vanilla = vanillaIds.get(type.key());
        if (vanilla != null) {
            return OptionalInt.of(vanilla);
        }
        final Registered custom = registered.get(type.key());
        return custom == null ? OptionalInt.empty() : OptionalInt.of(custom.networkId());
    }

    public int clientNetworkId(final Player player, final Key type) {
        final Integer vanilla = vanillaIds.get(type);
        if (vanilla != null) {
            return vanilla;
        }
        final Registered custom = registered.get(type);
        if (custom == null) {
            return -1;
        }
        if (custom.definition().isKnownBy(player)) {
            return custom.networkId();
        }
        final Optional<TypedKey<MobEffect>> fallback = custom.definition().fallback();
        if (fallback.isEmpty()) {
            return -1;
        }
        final Integer fallbackId = vanillaIds.get(fallback.get().key());
        return fallbackId == null ? -1 : fallbackId;
    }

    public List<MobEffectBehaviour> behaviours(final Key type) {
        final Registered custom = registered.get(type);
        final List<Attachment> attached = attachments.getOrDefault(type, List.of());
        if (custom == null && attached.isEmpty()) {
            return List.of();
        }
        final List<MobEffectBehaviour> behaviours = new ArrayList<>(attached.size() + 1);
        if (custom != null) {
            behaviours.add(custom.definition().behaviour());
        }
        for (final Attachment attachment : attached) {
            behaviours.add(attachment.behaviour());
        }
        return behaviours;
    }

    private boolean isVanilla(final TypedKey<MobEffect> type) {
        return vanillaIds.containsKey(type.key());
    }

    private static String ownerName(final Object owner) {
        return owner.getClass().getSimpleName();
    }

    private record Registered(MobEffectDefinition definition, Object owner, int networkId) {
    }

    private record Attachment(MobEffectBehaviour behaviour, Object owner) {
    }
}
