package fr.euphyllia.fidorial.server.command.brigadier.argument.selector.options;

/**
 * An option that may appear once, like {@code limit=} or {@code sort=}.
 */
public final class SingleUseOption {

    private boolean consumed;

    public boolean available() {
        return !consumed;
    }

    public void consume() {
        consumed = true;
    }
}
