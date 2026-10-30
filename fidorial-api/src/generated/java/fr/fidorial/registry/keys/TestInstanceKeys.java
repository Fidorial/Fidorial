package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.TestInstance;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:test_instance} registry.
 */
public final class TestInstanceKeys {
    /**
     * Key for {@code minecraft:always_pass}.
     */
    public static final TypedKey<TestInstance> ALWAYS_PASS = create("always_pass");

    private static final List<TypedKey<TestInstance>> VALUES = List.of(
        ALWAYS_PASS
    );

    private TestInstanceKeys() {
        throw new UnsupportedOperationException("TestInstanceKeys cannot be instantiated.");
    }

    private static TypedKey<TestInstance> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.TEST_INSTANCE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<TestInstance>> values() {
        return VALUES.stream();
    }
}
