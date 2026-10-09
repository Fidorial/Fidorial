package fr.fidorial.entity.mob;

import fr.fidorial.math.Location;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Where mobs are declared: the built-in ones, the ones plugins bend, and the ones they invent.
 *
 * @since 0.1.0
 */
public interface MobRegistry {

    /**
     * Declares a mob of its own.
     *
     * @param definition the mob to declare
     * @param owner      the plugin declaring it
     * @throws IllegalArgumentException if {@link MobDefinition#networkType()} is not a known entity type
     * @since 0.1.0
     */
    void register(MobDefinition definition, Object owner);

    /**
     * Attaches a behaviour to every mob of a kind spawned from now on.
     *
     * @param mobType the mob to bend, built-in or registered
     * @param factory builds the behaviour for each such mob
     * @param owner   the plugin attaching it
     * @since 0.1.0
     */
    void attach(Key mobType, MobBehaviour.Factory factory, Object owner);

    /**
     * Drops a definition declared through {@link #register(MobDefinition, Object)}.
     *
     * @param mobType the key to drop
     * @return {@code true} if a definition was registered under that key
     * @since 0.1.0
     */
    boolean unregister(Key mobType);

    /**
     * Drops the behaviours one plugin attached to a mob.
     *
     * @param mobType the mob to leave alone
     * @param owner   the plugin that attached them
     * @return {@code true} if at least one behaviour was dropped
     * @since 0.1.0
     */
    boolean detach(Key mobType, Object owner);

    /**
     * Drops every definition and behaviour a plugin declared.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Object owner);

    /**
     * @param mobType the key to look up
     * @return the definition registered under that key, empty for a built-in mob
     * @since 0.1.0
     */
    Optional<MobDefinition> definition(Key mobType);

    /**
     * @return every definition registered by a plugin
     * @since 0.1.0
     */
    Collection<MobDefinition> definitions();

    /**
     * @return every key that can be spawned, built-in mobs included
     * @since 0.1.0
     */
    Set<Key> types();

    /**
     * @param mobType the key to test
     * @return {@code true} when a mob can be spawned under that key
     * @since 0.1.0
     */
    boolean isMob(Key mobType);

    /**
     * Spawns a mob into the world.
     *
     * @param mobType  the mob to spawn
     * @param location where to put it
     * @return the mob, empty when the key is unknown or its implementation exposes no handle
     * @since 0.1.0
     */
    Optional<Mob> spawn(Key mobType, Location location);
}
