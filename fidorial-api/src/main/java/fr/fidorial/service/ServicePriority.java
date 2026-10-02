package fr.fidorial.service;

/**
 * The rank of a service implementation; when several are registered for the same service, the
 * highest one wins.
 *
 * <p>The server registers its own implementations at {@link #LOWEST}, so any plugin implementation
 * replaces them.</p>
 *
 * @since 0.1.0
 */
public enum ServicePriority {
    /**
     * Used by the server for its built-in implementations.
     */
    LOWEST,
    /**
     * Below the default.
     */
    LOW,
    /**
     * The default priority.
     */
    NORMAL,
    /**
     * Above the default.
     */
    HIGH,
    /**
     * Wins over every other implementation.
     */
    HIGHEST
}
