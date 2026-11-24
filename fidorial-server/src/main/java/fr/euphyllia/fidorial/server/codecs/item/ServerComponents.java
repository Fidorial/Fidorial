package fr.euphyllia.fidorial.server.codecs.item;

import fr.euphyllia.fidorial.server.combat.ChargedProjectiles;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Looks up the item components the server models: those plugins see in {@link DataComponentTypes},
 * and those the server keeps to itself, such as the projectiles a crossbow is loaded with.
 */
public final class ServerComponents {

    private static final Map<TypedKey<DataComponentType<?>>, DataComponentType<?>> SERVER_ONLY = Map.of(
            ChargedProjectiles.TYPE.key(), ChargedProjectiles.TYPE);

    private ServerComponents() {
        throw new UnsupportedOperationException("ServerComponents cannot be instantiated.");
    }

    /**
     * {@return the component type of a key, or {@code null} when the server does not model it}
     */
    public static @Nullable DataComponentType<?> byKey(final TypedKey<DataComponentType<?>> key) {
        final DataComponentType<?> type = DataComponentTypes.byKey(key);
        return type != null ? type : SERVER_ONLY.get(key);
    }

    /**
     * {@return the component type of a plain key, or {@code null} when the server does not model it}
     */
    public static @Nullable DataComponentType<?> byKey(final Key key) {
        return byKey(TypedKey.withRegistry(RegistryKey.DATA_COMPONENT_TYPE, key));
    }
}
