package fr.fidorial.world;

import fr.fidorial.Server;
import fr.fidorial.gamerule.GameRules;
import fr.fidorial.world.generation.WorldGenerator;
import fr.fidorial.world.structure.StructureManager;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * The worlds of the server: loading, creating and unloading them, and what they share (base game
 * rules, datapack structures).
 *
 * @see Server#worlds()
 * @since 0.1.0
 */
public interface WorldManager {

    /**
     * Gets the worlds currently loaded.
     *
     * @return the loaded worlds
     * @since 0.1.0
     */
    Collection<? extends World> all();

    /**
     * Looks up a loaded world.
     *
     * @param key the world key
     * @return the world, or empty if no world is loaded under that key
     * @since 0.1.0
     */
    default Optional<? extends World> get(final Key key) {
        return all().stream().filter(world -> world.key().equals(key)).findFirst();
    }

    /**
     * Creates a new world from the given specification.
     *
     * <p>If a world is already registered under the spec's {@linkplain WorldSpec#key() key}, that
     * existing world is returned unchanged and the rest of the spec (seed, generator) is ignored;
     * this call is therefore idempotent with respect to the world key. Otherwise a new world is
     * registered using the spec's {@linkplain WorldSpec#generator() generator}, or the server's
     * built-in default generator when the spec supplies none.</p>
     *
     * <p>The returned world is immediately usable: it participates in ticking and its chunks are
     * generated on demand through the configured generator.</p>
     *
     * @param spec the description of the world to create
     * @return the newly created world, or the existing world sharing the same key
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    World create(WorldSpec spec);

    /**
     * Creates a new world driven by the given generator, with a seed of {@code 0}.
     *
     * @param key       the world key
     * @param generator the chunk generator to drive the new world
     * @return the newly created world, or the existing world sharing the same key
     * @see #create(WorldSpec)
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    default World create(final Key key, final WorldGenerator generator) {
        return create(WorldSpec.builder(key).generator(generator).build());
    }

    /**
     * Unloads the world identified by {@code key}, optionally saving it to disk first.
     *
     * <p>Unloading removes the world from {@link #all()} and stops it being ticked; entities it
     * held are released. The call is refused, returning {@code false} without side effects, when:</p>
     * <ul>
     *   <li>no world is registered under {@code key};</li>
     *   <li>the world is the only one currently loaded; at least one world must always remain; or</li>
     *   <li>players are still present in the world; relocate them with another world first.</li>
     * </ul>
     *
     * @param key  the key of the world to unload
     * @param save whether to save the world before unloading it
     * @return {@code true} if the world was unloaded, {@code false} if the call was refused
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    boolean unload(Key key, boolean save);

    /**
     * {@return the key of the world currently preferred as the server's default, or {@linkplain Optional#empty()}
     * if none is configured}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<Key> defaultWorld();

    /**
     * Sets the world that should be preferred as the server's default when resolving a
     * fallback world for players and commands with no other context.
     *
     * @param key the key of the preferred world, or {@code null} to clear the preference
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void defaultWorld(@Nullable Key key);

    /**
     * The base game rules of the server: the overworld's, which every world follows unless it
     * overrides them through {@link World#gameRules()}.
     *
     * @return the base game rules, as changed by {@code /gamerule <rule> <value>}
     * @since 0.1.0
     */
    GameRules gameRules();

    /**
     * Datapack structures: packs of {@code <world>/datapacks}, templates, jigsaw structures, placement and
     * {@code /locate}-style search.
     *
     * @return the structure manager of the server
     * @since 0.1.0
     */
    StructureManager structures();
}
