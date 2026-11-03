package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;

record TypedKeyImpl<T>(RegistryKey<T> registry, Key key) implements TypedKey<T> {

    TypedKeyImpl {
        Preconditions.checkArgument(registry != null, "The registry of a typed key must not be null");
        Preconditions.checkArgument(key != null, "The key of a typed key must not be null");
    }

    @Override
    public T resolve(final Registry<T> registry) {
        return registry.get(this);
    }

    @Override
    public String toString() {
        return "TypedKey[" + registry.key().asString() + " / " + key.asString() + "]";
    }
}
