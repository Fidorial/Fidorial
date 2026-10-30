package fr.euphyllia.fidorial.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import org.jspecify.annotations.Nullable;

/**
 * An entry as sent in the registry data.
 *
 * @param data its data, {@code null} for a vanilla entry the client already knows
 */
public record RegistryEntry(Key key, @Nullable BinaryTag data) {
}
