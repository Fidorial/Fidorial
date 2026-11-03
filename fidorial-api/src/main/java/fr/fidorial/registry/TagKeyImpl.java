package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;

record TagKeyImpl<T>(RegistryKey<T> registry, Key key) implements TagKey<T> {

    TagKeyImpl {
        Preconditions.checkArgument(registry != null, "The registry of a tag key must not be null");
        Preconditions.checkArgument(key != null, "The key of a tag key must not be null");
    }

    @Override
    public String toString() {
        return "#" + key.asString();
    }
}
