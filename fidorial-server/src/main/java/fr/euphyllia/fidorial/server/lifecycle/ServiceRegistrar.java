package fr.euphyllia.fidorial.server.lifecycle;

/**
 * Registers the server's default implementation of an API service, at the lowest priority so
 * that plugins can replace it.
 */
public interface ServiceRegistrar {

    /**
     * @param service        the service type plugins look up
     * @param implementation the server's implementation
     * @param <T>            the service type
     */
    <T> void provide(Class<T> service, T implementation);
}
