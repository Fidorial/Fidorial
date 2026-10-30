package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.TestEnvironment;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:test_environment} registry.
 */
public final class TestEnvironmentKeys {
    /**
     * Key for {@code minecraft:default}.
     */
    public static final TypedKey<TestEnvironment> DEFAULT = create("default");

    private static final List<TypedKey<TestEnvironment>> VALUES = List.of(
        DEFAULT
    );

    private TestEnvironmentKeys() {
        throw new UnsupportedOperationException("TestEnvironmentKeys cannot be instantiated.");
    }

    private static TypedKey<TestEnvironment> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.TEST_ENVIRONMENT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<TestEnvironment>> values() {
        return VALUES.stream();
    }
}
