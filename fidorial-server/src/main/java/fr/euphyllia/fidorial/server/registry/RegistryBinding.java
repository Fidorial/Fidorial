package fr.euphyllia.fidorial.server.registry;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * How the values of a registry are made.
 *
 * @param <T> the value type
 */
public sealed interface RegistryBinding<T> {

    /**
     * Values decoded from data: vanilla entries lazily, and plugins may register more.
     *
     * @param codec     reads and writes the values
     * @param validator checks a value before it is registered
     */
    record Data<T>(RegistryCodec<T> codec, Validator<T> validator) implements RegistryBinding<T> {

        public Data {
            Objects.requireNonNull(codec, "codec");
            Objects.requireNonNull(validator, "validator");
        }

        public static <T> Data<T> of(final RegistryCodec<T> codec) {
            return new Data<>(codec, Validator.none());
        }
    }

    /**
     * Values made from their key alone; the registry is read-only.
     *
     * @param factory the value of a key, {@code null} when there is none
     */
    record Fixed<T>(Function<Key, @Nullable T> factory) implements RegistryBinding<T> {

        public Fixed {
            Objects.requireNonNull(factory, "factory");
        }
    }

    /**
     * Checks a value registered by a plugin.
     */
    interface Validator<T> {

        /**
         * @throws IllegalArgumentException if the value is invalid
         */
        void validate(ServerRegistries registries, Key key, T value);

        /**
         * {@return the tags the value refers to, created empty if missing so that clients find them}
         */
        default Set<Key> referencedTags(final T value) {
            return Set.of();
        }

        static <T> Validator<T> none() {
            return (_, _, _) -> {
            };
        }
    }
}
