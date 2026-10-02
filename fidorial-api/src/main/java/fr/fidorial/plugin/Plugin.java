package fr.fidorial.plugin;

/**
 * The entry point of a plugin, named by the {@code main} field of its {@code fidorial.json}.
 *
 * <p>The class needs a public no-argument constructor. The server calls the three lifecycle
 * methods in order, once each: {@link #onLoad(PluginContext)} as soon as the plugin is
 * instantiated, {@link #onEnable()} once every plugin is loaded, and {@link #onDisable()} when the
 * plugin is unloaded or the server stops. Subscriptions, services and permissions the plugin
 * registered are released after {@link #onDisable()}.</p>
 *
 * @since 0.1.0
 */
public interface Plugin {

    /**
     * Called right after the plugin is instantiated, before any other plugin is enabled.
     *
     * <p>Keep the context: it is the plugin's handle on the server for its whole life.</p>
     *
     * @param context the context of this plugin
     * @since 0.1.0
     */
    default void onLoad(final PluginContext context) {
    }

    /**
     * Called once every plugin has been loaded, in dependency order.
     *
     * @since 0.1.0
     */
    default void onEnable() {
    }

    /**
     * Called when the plugin is unloaded, including when the server stops.
     *
     * @since 0.1.0
     */
    default void onDisable() {
    }
}
