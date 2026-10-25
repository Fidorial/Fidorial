package fr.euphyllia.fidorial.server.item.enchantment.effect;

/**
 * Thrown when an enchantment effect is valid but cannot be evaluated by the server. The effect is
 * skipped on the server and still sent to clients.
 */
public final class UnsupportedEffectException extends RuntimeException {

    public UnsupportedEffectException(final String message) {
        super(message, null, false, false);
    }
}
