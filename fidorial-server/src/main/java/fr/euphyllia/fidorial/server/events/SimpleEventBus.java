package fr.euphyllia.fidorial.server.events;

import fr.fidorial.event.Event;
import fr.fidorial.event.EventBus;
import fr.fidorial.event.EventPriority;
import fr.fidorial.event.Subscription;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class SimpleEventBus implements EventBus {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(SimpleEventBus.class);

    private final Map<Class<?>, List<Registration<?>>> byType = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Registration<?>>> resolved = new ConcurrentHashMap<>();

    private final ThreadLocal<@Nullable Object> owner = new ThreadLocal<>();

    @Override
    public <E extends Event> Subscription subscribe(final Class<E> type, final EventPriority priority, final Consumer<E> listener) {
        final Registration<E> registration = new Registration<>(type, priority, listener, owner.get());
        byType.computeIfAbsent(type, _ -> new CopyOnWriteArrayList<>()).add(registration);
        resolved.clear();
        return registration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> E post(final E event) {
        for (final Registration<?> registration : resolve(event.getClass())) {
            if (!registration.active) {
                continue;
            }
            try {
                ((Registration<E>) registration).listener.accept(event);
            } catch (final Throwable t) {
                LOGGER.error(
                        "Listener en erreur sur {} (proprietaire : {})",
                        event.getClass().getSimpleName(),
                        registration.owner,
                        t);
            }
        }
        return event;
    }

    @Override
    public void unsubscribeAll(final Object owner) {
        for (final List<Registration<?>> registrations : byType.values()) {
            registrations.removeIf(r -> {
                final boolean match = r.owner == owner;
                if (match) {
                    r.active = false;
                }
                return match;
            });
        }
        resolved.clear();
    }

    public void withOwner(final Object pluginOwner, final Runnable action) {
        final Object previous = owner.get();
        owner.set(pluginOwner);
        try {
            action.run();
        } finally {
            if (previous == null) {
                owner.remove();
            } else {
                owner.set(previous);
            }
        }
    }

    private List<Registration<?>> resolve(final Class<?> eventType) {
        return resolved.computeIfAbsent(eventType, type -> {
            final List<Registration<?>> out = new ArrayList<>();
            collect(type, out);
            out.sort(Comparator.comparing(r -> r.priority));
            return List.copyOf(out);
        });
    }

    private void collect(@Nullable final Class<?> type, final List<Registration<?>> out) {
        if (type == null || !Event.class.isAssignableFrom(type)) {
            return;
        }
        final List<Registration<?>> direct = byType.get(type);
        if (direct != null) {
            out.addAll(direct);
        }
        collect(type.getSuperclass(), out);

        for (final Class<?> itf : type.getInterfaces()) {
            collect(itf, out);
        }
    }

    private final class Registration<E extends Event> implements Subscription {

        private final Class<E> type;
        private final EventPriority priority;
        private final Consumer<E> listener;
        private final @Nullable Object owner;
        private volatile boolean active = true;

        Registration(final Class<E> type, final EventPriority priority, final Consumer<E> listener, @Nullable final Object owner) {
            this.type = type;
            this.priority = priority;
            this.listener = listener;
            this.owner = owner;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public void unsubscribe() {
            if (!active) {
                return;
            }
            active = false;
            final List<Registration<?>> registrations = byType.get(type);
            if (registrations != null) {
                registrations.remove(this);
            }
            resolved.clear();
        }

        @Override
        public String toString() {
            return "Registration{" + "type="
                    + type + ", priority="
                    + priority + ", listener="
                    + listener + ", owner="
                    + owner + ", active="
                    + active + '}';
        }
    }
}
