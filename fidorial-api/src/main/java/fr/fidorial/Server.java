package fr.fidorial;

import fr.fidorial.command.CommandRegistry;
import fr.fidorial.entity.player.PlayerManager;
import fr.fidorial.event.EventBus;
import fr.fidorial.map.MapManager;
import fr.fidorial.plugin.PluginContext;
import fr.fidorial.plugin.PluginManager;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.scheduler.RegionizedScheduler;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.status.ServerListing;
import fr.fidorial.translation.TranslationStore;
import fr.fidorial.world.WorldManager;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.Contract;

/**
 * The running Fidorial server, entry point to every subsystem a plugin can reach.
 *
 * <p>A plugin obtains it through {@link PluginContext#server()}. Subsystems are grouped by domain:
 * {@link #players()}, {@link #worlds()}, {@link #registries()}, {@link #listing()}... As a
 * {@link ForwardingAudience}, the server forwards anything sent to it to every
 * {@linkplain PlayerManager#online() online player}.</p>
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
     * Gets how the server shows up in the multiplayer server list (description, favicon, maximum
     * player count).
     *
     * @return the server listing
     * @since 0.1.0
     */
    @Contract(pure = true)
    ServerListing listing();

    /**
     * Gets the players: who is online, every known identity, bans and whitelist.
     *
     * @return the players of the server
     * @since 0.1.0
     */
    @Contract(pure = true)
    PlayerManager players();

    /**
     * Gets the worlds: loading, creating and unloading them, base game rules and structures.
     *
     * @return the worlds of the server
     * @since 0.1.0
     */
    @Contract(pure = true)
    WorldManager worlds();

    /**
     * Gets the maps: the pictures behind {@code filled_map} items, terrain maps and canvases for
     * plugins to draw on.
     *
     * @return the maps of the server
     * @since 0.1.0
     */
    @Contract(pure = true)
    MapManager maps();

    /**
     * Gets the registries plugins can extend: biomes, dimension types, chat types, dialogs, mobs,
     * effects, items and permissions.
     *
     * @return the registries of the server
     * @since 0.1.0
     */
    @Contract(pure = true)
    GameRegistries registries();

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
     * <p>Plugins should prefer {@link PluginContext#events()}, whose
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
     * Gets the store rendering translatable components for a given locale.
     *
     * @return the active translation store
     * @since 0.1.0
     */
    TranslationStore translationStore();
}
