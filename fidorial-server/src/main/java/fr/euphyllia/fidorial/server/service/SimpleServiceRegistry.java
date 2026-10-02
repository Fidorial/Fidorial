package fr.euphyllia.fidorial.server.service;

import fr.fidorial.service.ServicePriority;
import fr.fidorial.service.ServiceRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleServiceRegistry implements ServiceRegistry {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(SimpleServiceRegistry.class);

    private final Map<Class<?>, List<Provider<?>>> providers = new ConcurrentHashMap<>();

    @Override
    public <T> void register(final Class<T> service, final T implementation, final Object owner, final ServicePriority priority) {
        if (!service.isInstance(implementation)) {
            throw new IllegalArgumentException(
                    implementation.getClass().getName() + " does not implement " + service.getName());
        }
        final List<Provider<?>> list = providers.computeIfAbsent(service, s -> new ArrayList<>());
        synchronized (list) {
            list.add(new Provider<>(implementation, owner, priority));
            list.sort(Comparator.comparing((Provider<?> p) -> p.priority).reversed());
        }
        LOGGER.debug(
                "Service {} provided by {} (priority {})",
                service.getSimpleName(),
                implementation.getClass().getName(),
                priority);
    }

    @Override
    public <T> T get(final Class<T> service) {
        return find(service)
                .orElseThrow(
                        () -> new IllegalStateException("No implementation recorded for " + service.getName()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(final Class<T> service) {
        final List<Provider<?>> list = providers.get(service);
        if (list == null) {
            return Optional.empty();
        }
        synchronized (list) {
            return list.isEmpty() ? Optional.empty() : Optional.of((T) list.getFirst().implementation);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(final Class<T> service) {
        final List<Provider<?>> list = providers.get(service);
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            return list.stream().map(p -> (T) p.implementation).toList();
        }
    }

    @Override
    public <T> boolean unregister(final Class<T> service, final T implementation) {
        final List<Provider<?>> list = providers.get(service);
        if (list == null) {
            return false;
        }
        synchronized (list) {
            return list.removeIf(p -> p.implementation == implementation);
        }
    }

    @Override
    public void unregisterAll(final Object owner) {
        for (final List<Provider<?>> list : providers.values()) {
            synchronized (list) {
                list.removeIf(p -> p.owner == owner);
            }
        }
    }

    private record Provider<T>(T implementation, Object owner, ServicePriority priority) {
    }
}
