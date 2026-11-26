package fr.euphyllia.fidorial.server.inventory;

import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * What happens to the items of the rows of a container no longer shown, chosen in the configuration:
 * the rows of an ender chest a player lost the permission of, the rows of a barrel beyond those the
 * configuration of its world gives.
 */
public enum HiddenRows {

    /**
     * The rows no longer shown are emptied when the container is opened: their items are lost, and
     * logged. What cannot be reached never piles up out of sight.
     */
    CLEAR("clear"),

    /**
     * The rows no longer shown keep their items, out of reach, until they are shown again.
     */
    KEEP("keep");

    private final String configName;

    HiddenRows(final String configName) {
        this.configName = configName;
    }

    /**
     * {@return the names of every choice, as written in the configuration}
     */
    public static String names() {
        final StringJoiner names = new StringJoiner(", ");
        for (final HiddenRows choice : values()) {
            names.add(choice.configName);
        }
        return names.toString();
    }

    /**
     * {@return the choice with that name, as written in the configuration, {@code null} for an
     * unknown name}: case and surrounding spaces do not matter
     *
     * @param name the name
     */
    public static @Nullable HiddenRows byName(final String name) {
        final String normalized = name.strip().toLowerCase(Locale.ROOT);
        for (final HiddenRows choice : values()) {
            if (choice.configName.equals(normalized)) {
                return choice;
            }
        }
        return null;
    }

    /**
     * {@return the name of the choice in the configuration}
     */
    public String configName() {
        return configName;
    }

    /**
     * {@return the stacks taken out of rows no longer shown, as the logs tell them: count and item,
     * one stack after the other}
     *
     * @param stacks the stacks cleared
     */
    static String describe(final List<ItemStack> stacks) {
        final StringJoiner joiner = new StringJoiner(", ");
        for (final ItemStack stack : stacks) {
            joiner.add(stack.count() + " " + stack.id().key().asString());
        }
        return joiner.toString();
    }
}
