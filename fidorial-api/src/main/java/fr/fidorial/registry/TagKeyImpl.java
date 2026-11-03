package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;

record TagKeyImpl<T>(RegistryKey<T> registry, Key key) implements TagKey<T> {

    TagKeyImpl {
        Preconditions.checkArgument(registry != null, "The registry of a tag key must not be null");
        Preconditions.checkArgument(key != null, "The key of a tag key must not be null");
        key = key.key();
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
