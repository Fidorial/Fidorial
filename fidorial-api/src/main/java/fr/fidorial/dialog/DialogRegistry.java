package fr.fidorial.dialog;

import fr.fidorial.Server;
import fr.fidorial.annotation.SinceMinecraft;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Optional;

/**
 * The dialogs registered on the server, sent to clients during configuration.
 *
 * @see Server#dialogs()
 * @since 0.1.0
 */
@SinceMinecraft("1.21.6")
public interface DialogRegistry {

    /**
     * Registers a dialog.
     *
     * @param key    the key to register it under
     * @param dialog the dialog to add
     * @return the registered dialog
     * @throws IllegalStateException if a dialog already sits under {@code key}; use
     *                               {@link #overwrite(Key, DialogDefinition)} to replace it on
     *                               purpose
     * @since 0.1.0
     */
    @Contract("_, _ -> param2")
    DialogDefinition register(Key key, DialogDefinition dialog);

    /**
     * Registers a dialog, replacing any dialog already sitting under the same key.
     *
     * @param key    the key to register it under
     * @param dialog the dialog to add or replace
     * @return the dialog previously registered under that key, if any
     * @since 0.1.0
     */
    Optional<DialogDefinition> overwrite(Key key, DialogDefinition dialog);

    /**
     * Removes a dialog.
     *
     * @param key the key to free
     * @return {@code true} if a dialog was removed
     * @since 0.1.0
     */
    boolean unregister(Key key);

    /**
     * {@return the dialog registered under {@code key}, when this server defines one}
     *
     * <p>The three dialogs the vanilla client ships with are known to the registry but carry no
     * definition of their own, so this returns empty for them while {@link #contains(Key)} returns
     * {@code true}.</p>
     *
     * @param key the key to look up
     * @since 0.1.0
     */
    Optional<DialogDefinition> definition(Key key);

    /**
     * {@return whether anything is registered under {@code key}}
     *
     * @param key the key to look up
     * @since 0.1.0
     */
    boolean contains(Key key);

    /**
     * {@return every registered key, in network order}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<Key> keys();

    /**
     * {@return the network identifier of {@code key}, or {@code -1} when it is not registered}
     *
     * @param key the key to look up
     * @since 0.1.0
     */
    int networkId(Key key);

    /**
     * Adds a dialog to the pause menu, under the {@code minecraft:pause_screen_additions} tag.
     *
     * <p>One tagged dialog is reachable directly; several are gathered behind the built-in
     * {@code minecraft:custom_options} screen, which lists them by
     * {@linkplain DialogBase#externalTitle() external title}.</p>
     *
     * @param key the key of an already registered dialog
     * @throws IllegalArgumentException if nothing is registered under {@code key}
     * @since 0.1.0
     */
    void addToPauseScreen(Key key);

    /**
     * Adds a dialog to the <em>Quick Actions</em> hotkey, under the
     * {@code minecraft:quick_actions} tag.
     *
     * @param key the key of an already registered dialog
     * @throws IllegalArgumentException if nothing is registered under {@code key}
     * @since 0.1.0
     */
    void addToQuickActions(Key key);

    /**
     * Drops a dialog from the pause menu and the quick actions hotkey.
     *
     * @param key the key to untag
     * @return {@code true} if the dialog carried at least one of the two tags
     * @since 0.1.0
     */
    boolean removeFromMenus(Key key);

    /**
     * {@return the dialogs reachable from the pause menu}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<Key> pauseScreenAdditions();

    /**
     * {@return the dialogs reachable from the quick actions hotkey}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<Key> quickActions();

    /**
     * Registers a dialog.
     *
     * @param key    the typed key to register it under
     * @param dialog the dialog to add
     * @return the registered dialog
     * @throws IllegalStateException if a dialog already sits under {@code key}
     * @since 0.1.0
     */
    @Contract("_, _ -> param2")
    default DialogDefinition register(final TypedKey<fr.fidorial.registry.data.Dialog> key, final DialogDefinition dialog) {
        return register(key.key(), dialog);
    }

    /**
     * Reads a dialog from its data pack JSON representation and registers it.
     *
     * <p>The JSON is the very same one a data pack would drop in
     * {@code data/<namespace>/dialog/<name>.json}.</p>
     *
     * @param key  the key to register it under
     * @param json the JSON object describing the dialog
     * @return the registered dialog
     * @throws IllegalArgumentException if the JSON is malformed or misses a mandatory field
     * @throws IllegalStateException    if a dialog already sits under {@code key}
     * @since 0.1.0
     */
    DialogDefinition registerFromJson(Key key, String json);
}
