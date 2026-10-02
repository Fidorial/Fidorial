package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;


/**
 * A pointer to a dialog held by the {@linkplain DialogRegistry dialog registry}.
 *
 * @param key the key the dialog is registered under
 * @see Dialog#reference(Key)
 * @since 0.1.0
 */
public record DialogReference(Key key) implements Dialog {

    /**
     * @param key the key the dialog is registered under
     * @since 0.1.0
     */
    public DialogReference {
        Preconditions.checkArgument(key != null, "The key of a dialog reference must not be null");
    }
}
