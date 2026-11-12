package fr.fidorial.registry;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

record TypedKeyImpl<T>(RegistryKey<T> registry, Key key) implements TypedKey<T> {

    TypedKeyImpl {
        Preconditions.checkArgument(registry != null, "The registry of a typed key must not be null");
        Preconditions.checkArgument(key != null, "The key of a typed key must not be null");
    }

    @Override
    public T resolve(final Registry<T> registry) {
        return registry.get(this);
    }

    // written out so nested key hashes inline; the generated ones hit the recursive inlining limit
    @Override
    public boolean equals(final @Nullable Object other) {
        //noinspection DeconstructionCanBeUsed
        return this == other
                || (other instanceof final TypedKeyImpl<?> that && key.equals(that.key()) && registry.equals(that.registry()));
    }

    @Override
    public int hashCode() {
        return 31 * registry.hashCode() + key.hashCode();
    }

    @Override
    public String toString() {
        return "TypedKey[" + registry.key().asString() + " / " + key.asString() + "]";
    }
}
