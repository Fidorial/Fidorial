package fr.fidorial.registry.keys;

import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:dialog} registry.
 */
public final class DialogTagKeys {
    /**
     * Tag {@code #minecraft:pause_screen_additions}.
     */
    public static final TagKey<DialogDefinition> PAUSE_SCREEN_ADDITIONS = create("pause_screen_additions");

    /**
     * Tag {@code #minecraft:quick_actions}.
     */
    public static final TagKey<DialogDefinition> QUICK_ACTIONS = create("quick_actions");

    private DialogTagKeys() {
        throw new UnsupportedOperationException("DialogTagKeys cannot be instantiated.");
    }

    private static TagKey<DialogDefinition> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.DIALOG, value);
    }
}
