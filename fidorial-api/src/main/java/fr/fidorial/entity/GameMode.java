package fr.fidorial.entity;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * The vanilla game modes, with their protocol identifiers.
 *
 * @since 0.1.0
 */
public enum GameMode {
    /**
     * Players gather resources, take damage and can die.
     */
    SURVIVAL(0),
    /**
     * Players fly, are invulnerable and have unlimited blocks.
     */
    CREATIVE(1),
    /**
     * Like survival, but blocks can only be broken or placed where items allow it.
     */
    ADVENTURE(2),
    /**
     * Players fly through blocks and cannot interact with the world.
     */
    SPECTATOR(3);

    private final int id;

    GameMode(final int id) {
        this.id = id;
    }

    /**
     * {@return the game mode with a protocol identifier, or {@code null} if there is none}
     *
     * @param id the protocol identifier, from {@code 0} to {@code 3}
     * @since 0.1.0
     */
    public static @Nullable GameMode byId(final int id) {
        for (final GameMode mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return null;
    }

    /**
     * Parses a game mode as typed in a command.
     *
     * <p>Accepts the English name, its French translation, the usual abbreviation ({@code s},
     * {@code c}, {@code a}, {@code sp}) or the protocol identifier, ignoring case.</p>
     *
     * @param input the text to parse
     * @return the game mode, or {@code null} if the text matches none
     * @since 0.1.0
     */
    public static @Nullable GameMode byName(final String input) {
        if (input.isBlank()) {
            return null;
        }
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "survival", "survie", "s", "0" -> SURVIVAL;
            case "creative", "creatif", "créatif", "c", "1" -> CREATIVE;
            case "adventure", "aventure", "a", "2" -> ADVENTURE;
            case "spectator", "spectateur", "sp", "3" -> SPECTATOR;
            default -> null;
        };
    }

    /**
     * {@return the protocol identifier of this game mode}
     *
     * @since 0.1.0
     */
    public int id() {
        return id;
    }
}
