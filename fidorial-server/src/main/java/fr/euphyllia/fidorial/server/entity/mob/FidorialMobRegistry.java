package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.entity.mob.MobBehaviour;
import fr.fidorial.entity.mob.MobDefinition;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FidorialMobRegistry implements MobRegistry {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialMobRegistry.class);

    private final Map<Key, MobDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<Key, Object> definitionOwners = new ConcurrentHashMap<>();
    private final Map<Key, List<Attachment>> attachments = new ConcurrentHashMap<>();

    private final Set<Key> syntheticTypes = ConcurrentHashMap.newKeySet();

    @Override
    public void register(final MobDefinition definition, final Object owner) {
        final EntityType networkType = EntityTypes.get(definition.networkType());
        if (networkType == null || !EntityTypes.hasNetworkId(networkType)) {
            throw new IllegalArgumentException(
                    "Unknown network entity type " + definition.networkType() + " for the mob " + definition.key());
        }

        final boolean known = EntityTypes.get(definition.key()) != null && !syntheticTypes.contains(definition.key());
        if (!known) {
            EntityTypes.registerCustom(
                    new EntityType(
                            definition.key(),
                            networkType.category(),
                            (float) definition.width(),
                            (float) definition.height()),
                    EntityTypes.networkId(networkType));
            syntheticTypes.add(definition.key());
        }

        definitions.put(definition.key(), definition);
        definitionOwners.put(definition.key(), owner);
        LOGGER.info("Mob {} registered by {} (rendered as {})",
                definition.key(), ownerName(owner), definition.networkType());
    }

    @Override
    public void attach(final Key mobType, final MobBehaviour.Factory factory, final Object owner) {
        attachments
                .computeIfAbsent(mobType, _ -> new CopyOnWriteArrayList<>())
                .add(new Attachment(factory, owner));
        LOGGER.debug("Behaviour attached to {} by {}", mobType, ownerName(owner));
    }

    @Override
    public boolean unregister(final Key mobType) {
        final MobDefinition removed = definitions.remove(mobType);
        if (removed == null) {
            return false;
        }
        definitionOwners.remove(mobType);
        if (syntheticTypes.remove(mobType)) {
            EntityTypes.unregister(mobType);
        }
        LOGGER.info("Mob {} unregistered", removed.key());
        return true;
    }

    @Override
    public boolean detach(final Key mobType, final Object owner) {
        final List<Attachment> list = attachments.get(mobType);
        if (list == null) {
            return false;
        }
        final boolean removed = list.removeIf(attachment -> attachment.owner() == owner);
        if (list.isEmpty()) {
            attachments.remove(mobType);
        }
        return removed;
    }

    @Override
    public void unregisterAll(final Object owner) {
        for (final Map.Entry<Key, Object> entry : definitionOwners.entrySet()) {
            if (entry.getValue() == owner) {
                unregister(entry.getKey());
            }
        }
        for (final Key mobType : Set.copyOf(attachments.keySet())) {
            detach(mobType, owner);
        }
    }

    @Override
    public Optional<MobDefinition> definition(final Key mobType) {
        return Optional.ofNullable(definitions.get(mobType));
    }

    @Override
    public Collection<MobDefinition> definitions() {
        return List.copyOf(definitions.values());
    }

    @Override
    public Set<Key> types() {
        final Set<Key> keys = new LinkedHashSet<>(MobFactories.builtInKeys());
        keys.addAll(definitions.keySet());
        return Set.copyOf(keys);
    }

    @Override
    public boolean isMob(final Key mobType) {
        return definitions.containsKey(mobType) || MobFactories.isBuiltIn(mobType);
    }

    @Override
    public Optional<Mob> spawn(final Key mobType, final Location location) {
        final EntityType type = EntityTypes.get(mobType);
        if (type == null || !isMob(mobType)) {
            return Optional.empty();
        }
        final FidorialServer server = FidorialServer.getInstance();
        final AbstractMob mob = MobFactories.create(type, server.entityIds().allocate(), location);
        server.spawnEntity(mob);
        return mob instanceof final Mob handle ? Optional.of(handle) : Optional.empty();
    }


    public @Nullable PluginMob createDefined(final EntityType type, final int entityId, final Location location) {
        final MobDefinition definition = definitions.get(type.key());
        return definition == null ? null : new PluginMob(definition, type, entityId, location);
    }

    public void applyBehaviours(final AbstractMob mob) {
        if (!(mob instanceof final Mob handle)) {
            return;
        }

        final MobDefinition definition = definitions.get(mob.type().key());
        if (definition != null) {
            create(definition.behaviour(), handle, mob, definition.key());
        }

        final List<Attachment> attached = attachments.get(mob.type().key());
        if (attached == null) {
            return;
        }
        for (final Attachment attachment : attached) {
            create(attachment.factory(), handle, mob, mob.type().key());
        }
    }

    private static void create(final MobBehaviour.Factory factory, final Mob handle,
                               final AbstractMob mob, final Key key) {
        try {
            final MobBehaviour behaviour = factory.create(handle);
            mob.addBehaviour(behaviour);
        } catch (final Throwable throwable) {
            LOGGER.error("A behaviour of {} could not be created", key, throwable);
        }
    }

    private static String ownerName(final Object owner) {
        return owner.getClass().getName();
    }

    private record Attachment(MobBehaviour.Factory factory, Object owner) {
    }
}
