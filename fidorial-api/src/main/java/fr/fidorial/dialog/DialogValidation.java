package fr.fidorial.dialog;

import com.google.common.base.Preconditions;

/**
 * Argument checks shared by the dialog records.
 */
final class DialogValidation {

    private DialogValidation() {
        throw new UnsupportedOperationException("DialogValidation cannot be instantiated.");
    }

    static int width(final int value, final int max, final String name) {
        Preconditions.checkArgument(value >= 1 && value <= max, "The %s must be between 1 and %s, got %s", name, max, value);
        return value;
    }

    static String inputKey(final String key) {
        Preconditions.checkArgument(!key.isEmpty(), "An input key cannot be empty");
        for (int i = 0; i < key.length(); i++) {
            final char c = key.charAt(i);
            final boolean allowed = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '_';
            Preconditions.checkArgument(allowed, "An input key may only contain letters, digits and '_', got '%s'", key);
        }
        return key;
    }

    static int positive(final int value, final String name) {
        Preconditions.checkArgument(value >= 1, "The %s must be positive, got %s", name, value);
        return value;
    }
}
