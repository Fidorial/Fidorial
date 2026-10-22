package fr.euphyllia.fidorial.server.command.brigadier.argument.selector.options;

/**
 * An option that takes either one positive value or any number of negated ones, like {@code type=} or {@code !type=}.
 */
public final class SelectorSetState {

    private boolean positive;
    private boolean negative;

    public boolean canAdd(final boolean inverted) {
        return !positive && (inverted || !negative);
    }

    public boolean canAddAny() {
        return !positive;
    }

    public void add(final boolean inverted) {
        if (inverted) {
            negative = true;
        } else {
            positive = true;
        }
    }
}
