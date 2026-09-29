package fr.euphyllia.fidorial.server.codecs;

import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RecordShapes {

    private RecordShapes() {
        throw new UnsupportedOperationException("RecordShapes cannot be instantiated.");
    }

    /**
     * @param keys one entry per declared field, in declaration order; {@code null} for fields without a key
     */
    public static void checkKeys(final Class<? extends Record> type, final List<@Nullable String> keys, final RecordCodec.KeyStyle style) {
        final RecordComponent[] components = type.getRecordComponents();
        if (components.length != keys.size()) {
            throw new IllegalStateException(type.getName() + " has " + components.length + " components but " + keys.size() + " fields were declared");
        }
        final Set<String> seen = new HashSet<>();
        for (int i = 0; i < components.length; i++) {
            final String key = keys.get(i);
            if (key == null) {
                continue;
            }
            final String expected = style.keyFor(components[i].getName());
            if (expected != null && !key.substring(key.lastIndexOf('/') + 1).equals(expected)) {
                throw new IllegalStateException("Field #" + (i + 1) + " '" + key + "' of " + type.getName()
                        + " does not match component '" + components[i].getName() + "' (expected '" + expected
                        + "'); fields must be declared in component order");
            }
            if (!seen.add(key)) {
                throw new IllegalStateException("Duplicate key '" + key + "' in " + type.getName());
            }
        }
    }

    public static MethodHandle canonicalConstructor(final Class<? extends Record> type) {
        final Class<?>[] parameters = Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        try {
            final Constructor<? extends Record> constructor = type.getDeclaredConstructor(parameters);
            constructor.trySetAccessible();
            return MethodHandles.lookup().unreflectConstructor(constructor);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access the canonical constructor of " + type.getName(), e);
        }
    }
}
