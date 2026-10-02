package fr.fidorial;

import fr.fidorial.chat.ChatTypeRegistry;
import fr.fidorial.command.CommandRegistry;
import fr.fidorial.dialog.DialogRegistry;
import fr.fidorial.entity.OfflinePlayers;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.mob.MobRegistry;
import fr.fidorial.event.EventBus;
import fr.fidorial.gamerule.GameRules;
import fr.fidorial.item.ItemRegistry;
import fr.fidorial.moderation.BanManager;
import fr.fidorial.moderation.WhitelistManager;
import fr.fidorial.permission.PermissionRegistry;
import fr.fidorial.plugin.PluginContext;
import fr.fidorial.plugin.PluginManager;
import fr.fidorial.scheduler.RegionizedScheduler;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.status.Favicon;
import fr.fidorial.translation.TranslationStore;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldSpec;
import fr.fidorial.world.biome.BiomeRegistry;
import fr.fidorial.world.dimension.DimensionTypeRegistry;
import fr.fidorial.world.generation.WorldGenerator;
import fr.fidorial.world.structure.StructureManager;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The running Fidorial server, entry point to every subsystem a plugin can reach.
 *
 * <p>A plugin obtains it through {@link PluginContext#server()}. As a
 * {@link ForwardingAudience}, the server forwards anything sent to it to every
 * {@linkplain #onlinePlayers() online player}.</p>
 *
 * @since 0.1.0
 */
public interface Server extends ForwardingAudience {
    /**
     * Gets the server brand name.
     *
     * @return server brand name
     * @since 0.1.0
     */
    @Contract(pure = true)
    String brandName();

    /**
     * Gets the version of Minecraft this server implements.
     *
     * @return the Minecraft version, for instance {@code 26.3}
     * @since 0.1.0
     */
    @Contract(pure = true)
    String minecraftVersion();

    /**
     * Gets the network protocol version this server speaks.
     *
     * @return the protocol version clients must use to join
     * @since 0.1.0
     */
    @Contract(pure = true)
    int protocolVersion();

    /**
     * Checks whether the server is running.
     *
     * @return {@code true} until {@link #shutdown()} has been requested
     * @since 0.1.0
     */
    boolean isRunning();

    /**
     * Stops the server: fires {@link fr.fidorial.event.server.ServerStoppingEvent}, kicks every
     * player, disables the plugins and saves the worlds.
     *
     * <p>Calling it again once the shutdown has started does nothing.</p>
     *
     * @since 0.1.0
     */
    void shutdown();

    /**
     * Gets the server's favicon shown in the status ping.
     *
     * @return the server favicon
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<Favicon> favicon();

    /**
     * Sets the server's favicon shown in the status ping.
     *
     * @param favicon the server favicon
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void favicon(Favicon favicon);

    /**
     * Gets the server description shown in the status ping.
     *
     * @return server description
     * @since 0.1.0
     */
    @Contract(pure = true)
    Component description();

    /**
     * Sets the server description shown in the status ping.
     *
     * @param description server description
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void description(Component description);

    /**
     * Gets the maximum player count shown in the status ping.
     *
     * @return maximum player count
     * @since 0.1.0
     */
    @Contract(pure = true)
    int maxPlayers();

    /**
     * Sets the maximum player count shown in the status ping.
     *
     * @param maxPlayers maximum player count
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void maxPlayers(int maxPlayers);

    /**
     * Gets the current online player count.
     *
     * @return online player count
     * @since 0.1.0
     */
    @Contract(pure = true)
    int playerCount();

    /**
     * Gets the players currently connected.
     *
     * @return the online players
     * @since 0.1.0
     */
    Collection<? extends Player> onlinePlayers();

    /**
     * Looks up an online player by identity.
     *
     * @param uuid the player identity
     * @return the player, or empty if no player with that identity is connected
     * @since 0.1.0
     */
    Optional<? extends Player> player(UUID uuid);

    /**
     * Looks up an online player by name, ignoring case.
     *
     * @param name the player name
     * @return the player, or empty if no player with that name is connected
     * @since 0.1.0
     */
    Optional<? extends Player> player(String name);

    /**
     * Gets the directory of every identity the server knows of, connected or not.
     *
     * @return the offline player directory
     * @since 0.1.0
     */
    @Contract(pure = true)
    OfflinePlayers offlinePlayers();

    /**
     * Gets the service holding the players who are not allowed to connect.
     *
     * @return the ban service
     * @since 0.1.0
     */
    @Contract(pure = true)
    BanManager bans();

    /**
     * Gets the service holding the players allowed to connect while the whitelist is enforced.
     *
     * @return the whitelist service
     * @since 0.1.0
     */
    @Contract(pure = true)
    WhitelistManager whitelist();

    /**
     * Gets the worlds currently loaded.
     *
     * @return the loaded worlds
     * @since 0.1.0
     */
    Collection<? extends World> worlds();

    /**
     * Looks up a loaded world.
     *
     * @param key the world key
     * @return the world, or empty if no world is loaded under that key
     * @since 0.1.0
     */
    Optional<? extends World> world(Key key);

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
    World createWorld(WorldSpec spec);

    /**
     * Creates a new world driven by the given generator, with a seed of {@code 0}.
     *
     * @param key       the world key
     * @param generator the chunk generator to drive the new world
     * @return the newly created world, or the existing world sharing the same key
     * @see #createWorld(WorldSpec)
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    default World createWorld(final Key key, final WorldGenerator generator) {
        return createWorld(WorldSpec.builder(key).generator(generator).build());
    }

    /**
     * Unloads the world identified by {@code key}, optionally saving it to disk first.
     *
     * <p>Unloading removes the world from {@link #worlds()} and stops it being ticked; entities it
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
    boolean unloadWorld(Key key, boolean save);

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

    /**
     * Gets the scheduler running tasks on the region threads.
     *
     * @return the regionized scheduler
     * @since 0.1.0
     */
    @Contract(pure = true)
    RegionizedScheduler scheduler();

    /**
     * Gets the server-wide event bus.
     *
     * <p>Plugins should prefer {@link fr.fidorial.plugin.PluginContext#events()}, whose
     * subscriptions are released automatically when the plugin is disabled.</p>
     *
     * @return the event bus
     * @since 0.1.0
     */
    @Contract(pure = true)
    EventBus events();

    /**
     * Gets the registry of services, through which plugins replace the default server behaviour.
     *
     * @return the service registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    ServiceRegistry services();

    /**
     * Gets the plugin loader.
     *
     * @return the plugin manager
     * @since 0.1.0
     */
    @Contract(pure = true)
    PluginManager plugins();

    /**
     * Gets the registry of Brigadier commands.
     *
     * @return the command registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    CommandRegistry commands();

    /**
     * Gets the server-wide permission registry.
     *
     * @return the permission registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    PermissionRegistry permissions();

    /**
     * Gets the store rendering translatable components for a given locale.
     *
     * @return the active translation store
     * @since 0.1.0
     */
    TranslationStore translationStore();

    /**
     * Gets the server-wide dialog registry.
     *
     * @return the dialog registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    DialogRegistry dialogs();

    /**
     * Gets the server-wide biome registry.
     *
     * <p>Biomes registered there are sent to every client, and may be referenced
     * by {@link fr.fidorial.world.generation.WorldGenerator generators} through
     * {@link fr.fidorial.world.generation.GeneratedChunk#setBiome(int, int, int, Key)}.</p>
     *
     * @return the biome registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    BiomeRegistry biomes();

    /**
     * Gets the server-wide dimension type registry.
     *
     * <p>Dimension types registered there are sent to every client, and may be returned by
     * {@link WorldGenerator#dimensionType() generators} to shape the worlds they drive.</p>
     *
     * @return the dimension type registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    DimensionTypeRegistry dimensionTypes();

    /**
     * Gets the server-wide chat type registry.
     *
     * @return the chat type registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    ChatTypeRegistry chatTypes();

    /**
     * Gets the server-wide mob registry.
     *
     * <p>Where plugins {@linkplain MobRegistry#attach attach behaviours} to
     * the built-in mobs, and {@linkplain MobRegistry#register register}
     * mobs of their own.</p>
     *
     * @return the mob registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    MobRegistry mobs();

    /**
     * Gets the server-wide item registry.
     *
     * <p>Where plugins {@linkplain ItemRegistry#register register} items of their own,
     * rendered client-side as the vanilla item they name.</p>
     *
     * @return the item registry
     * @since 0.1.0
     */
    @Contract(pure = true)
    ItemRegistry items();
}
