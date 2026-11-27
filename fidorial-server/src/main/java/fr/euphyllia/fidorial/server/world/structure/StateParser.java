package fr.euphyllia.fidorial.server.world.structure;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.BlockStateCodecs;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StateParser {

    private StateParser() {
    }

    public record Parsed(TypedKey<BlockType> name, Map<String, String> properties) {
    }

    private static @Nullable TypedKey<BlockType> block(final @Nullable String raw) {
        final Key key = Keys.parse(raw);
        return key == null ? null : TypedKey.withRegistry(RegistryKey.BLOCK, key);
    }

    public static @Nullable Parsed parseString(final String raw) {
        String text = raw.strip();
        final int nbtStart = text.indexOf('{');
        if (nbtStart >= 0) {
            text = text.substring(0, nbtStart);
        }
        final Map<String, String> properties = new LinkedHashMap<>();
        final int open = text.indexOf('[');
        String name = text;
        if (open >= 0) {
            name = text.substring(0, open);
            final int close = text.lastIndexOf(']');
            final String body = text.substring(open + 1, close > open ? close : text.length());
            for (final String pair : Splitter.on(',').split(body)) {
                final int eq = pair.indexOf('=');
                if (eq > 0) {
                    properties.put(pair.substring(0, eq).strip(), pair.substring(eq + 1).strip());
                }
            }
        }
        final TypedKey<BlockType> key = block(name);
        return key == null ? null : new Parsed(key, properties);
    }

    public static @Nullable BlockState parseJson(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return BlockStateCodecs.STATE.parse(JsonOps.INSTANCE, element).result().map(BlockState::of).orElse(null);
    }

    public static @Nullable BlockState resolve(final @Nullable Parsed parsed, final BlockValidator validator) {
        return parsed == null ? null : validator.resolve(parsed.name(), parsed.properties());
    }
}
