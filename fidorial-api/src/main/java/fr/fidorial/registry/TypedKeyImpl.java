package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;

record TypedKeyImpl<T>(RegistryKey<T> registry, Key key) implements TypedKey<T> {

    TypedKeyImpl {
        Preconditions.checkArgument(registry != null, "The registry of a typed key must not be null");
        Preconditions.checkArgument(key != null, "The key of a typed key must not be null");
        key = key.key();
    }

    @Override
    public T resolve(final Registry<T> registry) {
        return registry.get(this);
    }

    @Override
    public String namespace() {
        return key.namespace();
    }

    @Override
    public String value() {
        return key.value();
    }

    @Override
    public String asString() {
        return key.asString();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof final Key that && key.equals(that.key());
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key.asString();
    }
}
