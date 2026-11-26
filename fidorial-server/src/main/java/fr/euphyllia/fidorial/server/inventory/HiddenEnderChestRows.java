package fr.euphyllia.fidorial.server.inventory;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * What happens to the items of the rows of an ender chest a player no longer sees, because they lost
 * the permission of those rows, chosen in the server configuration.
 */
public enum HiddenEnderChestRows {

    /**
     * The rows a player does not see are emptied when they open their ender chest: their items are
     * lost. What a player cannot reach never piles up out of sight.
     */
    CLEAR("clear"),

    /**
     * The rows a player does not see keep their items, out of reach, until the player is granted
     * those rows again.
     */
    KEEP("keep");

    private final String configName;

    HiddenEnderChestRows(final String configName) {
        this.configName = configName;
    }

    /**
     * {@return the names of every choice, as written in the configuration}
     */
    public static String names() {
        return Arrays.stream(values()).map(HiddenEnderChestRows::configName).collect(Collectors.joining(", "));
    }

    /**
     * {@return the choice with that name, as written in the configuration, {@code null} for an
     * unknown name}: case and surrounding spaces do not matter
     *
     * @param name the name
     */
    public static @Nullable HiddenEnderChestRows byName(final String name) {
        final String normalized = name.strip().toLowerCase(Locale.ROOT);
        for (final HiddenEnderChestRows choice : values()) {
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
}
