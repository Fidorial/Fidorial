package fr.fidorial.service;

import fr.fidorial.combat.CombatService;

import java.util.List;
import java.util.Optional;

/**
 * Holds the implementations of the services through which the server delegates its behaviour.
 *
 * <p>A service is identified by an interface, for instance
 * {@link CombatService}. Several implementations may be registered for the same
 * service; the one with the highest {@link ServicePriority} is used, the first registered one
 * winning a tie.</p>
 *
 * @since 0.1.0
 */
public interface ServiceRegistry {

    /**
     * Registers an implementation of a service.
     *
     * @param service        the service interface
     * @param implementation the implementation
     * @param owner          the owner of the registration, usually the {@link fr.fidorial.plugin.Plugin}
     *                       instance; it is released by {@link #unregisterAll(Object)}
     * @param priority       the rank of this implementation
     * @param <T>            the service type
     * @throws IllegalArgumentException if {@code implementation} does not implement {@code service}
     * @since 0.1.0
     */
    <T> void register(Class<T> service, T implementation, Object owner, ServicePriority priority);

    /**
     * Registers an implementation of a service at {@link ServicePriority#NORMAL} priority.
     *
     * @param service        the service interface
     * @param implementation the implementation
     * @param owner          the owner of the registration, usually the {@link fr.fidorial.plugin.Plugin} instance
     * @param <T>            the service type
     * @throws IllegalArgumentException if {@code implementation} does not implement {@code service}
     * @since 0.1.0
     */
    default <T> void register(final Class<T> service, final T implementation, final Object owner) {
        register(service, implementation, owner, ServicePriority.NORMAL);
    }

    /**
     * Gets the implementation in use for a service.
     *
     * @param service the service interface
     * @param <T>     the service type
     * @return the implementation with the highest priority
     * @throws IllegalStateException if no implementation is registered
     * @since 0.1.0
     */
    <T> T get(Class<T> service);

    /**
     * Gets the implementation in use for a service, if any.
     *
     * @param service the service interface
     * @param <T>     the service type
     * @return the implementation with the highest priority, or empty if none is registered
     * @since 0.1.0
     */
    <T> Optional<T> find(Class<T> service);

    /**
     * Gets every implementation registered for a service, for instance to delegate to the one a
     * plugin replaced.
     *
     * @param service the service interface
     * @param <T>     the service type
     * @return an immutable snapshot of the implementations, the one in use first
     * @since 0.1.0
     */
    <T> List<T> findAll(Class<T> service);

    /**
     * Removes one implementation of a service.
     *
     * @param service        the service interface
     * @param implementation the implementation to remove
     * @param <T>            the service type
     * @return {@code true} if the implementation was registered
     * @since 0.1.0
     */
    <T> boolean unregister(Class<T> service, T implementation);

    /**
     * Removes every implementation registered by {@code owner}, whatever the service.
     *
     * @param owner the owner given at registration
     * @since 0.1.0
     */
    void unregisterAll(Object owner);
}
