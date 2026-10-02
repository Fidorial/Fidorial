package fr.fidorial.plugin;

import fr.fidorial.Server;
import fr.fidorial.event.EventBus;
import fr.fidorial.service.ServiceRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Everything a plugin receives from the server, handed over in {@link Plugin#onLoad(PluginContext)}.
 *
 * @since 0.1.0
 */
public interface PluginContext {

    /**
     * {@return the metadata read from the plugin's {@code fidorial.json}}
     *
     * @since 0.1.0
     */
    PluginMeta meta();

    /**
     * {@return the running server}
     *
     * @since 0.1.0
     */
    Server server();

    /**
     * Gets the event bus bound to this plugin.
     *
     * <p>Every subscription made through it belongs to the plugin and is released when the plugin is
     * disabled, whatever the thread it was made from.</p>
     *
     * @return the plugin-bound event bus
     * @since 0.1.0
     */
    EventBus events();

    /**
     * Gets the service registry.
     *
     * <p>Pass the {@link Plugin} instance as the owner of what you register, so it is released when the
     * plugin is disabled.</p>
     *
     * @return the service registry
     * @since 0.1.0
     */
    ServiceRegistry services();

    /**
     * {@return the logger of this plugin, named {@code plugin/<id>}}
     *
     * @since 0.1.0
     */
    ComponentLogger logger();

    /**
     * Gets the folder reserved for this plugin's files, {@code plugins/<id>}, creating it when missing.
     *
     * @return the data folder
     * @throws UncheckedIOException if the folder cannot be created
     * @since 0.1.0
     */
    Path dataFolder();

    /**
     * Opens a resource bundled inside this plugin's jar, relative to the jar root.
     *
     * @param path path to the resource, relative to the jar root
     * @return an input stream for the resource, or {@code null} if no entry exists at that path
     * @apiNote The caller owns the returned stream and is responsible for closing it, typically via try-with-resources
     * @since 0.1.0
     */
    @Nullable InputStream resource(String path);
}
