package fr.euphyllia.fidorial.server.world.structure;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public final class Keys {

    public static final TypedKey<BlockType> AIR = BlockTypeKeys.AIR;
    public static final TypedKey<BlockType> CAVE_AIR = BlockTypeKeys.CAVE_AIR;
    public static final TypedKey<BlockType> VOID_AIR = BlockTypeKeys.VOID_AIR;
    public static final TypedKey<BlockType> WATER = BlockTypeKeys.WATER;
    public static final TypedKey<BlockType> LAVA = BlockTypeKeys.LAVA;
    public static final TypedKey<BlockType> JIGSAW = BlockTypeKeys.JIGSAW;
    public static final TypedKey<BlockType> STRUCTURE_VOID = BlockTypeKeys.STRUCTURE_VOID;
    public static final TypedKey<BlockType> STRUCTURE_BLOCK = BlockTypeKeys.STRUCTURE_BLOCK;
    // Not backed by a registry afaik
    public static final Key EMPTY = Key.key("empty");
    public static final Key BOTTOM = Key.key("bottom");

    private Keys() {
    }

    public static @Nullable Key parse(final @Nullable String raw) {
        if (raw == null) {
            return null;
        }
        final String trimmed = raw.strip();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Key.key(trimmed);
        } catch (final InvalidKeyException | IllegalArgumentException _) {
            return null;
        }
    }

    public static Key parseOr(final @Nullable String raw, final Key fallback) {
        final Key key = parse(raw);
        return key == null ? fallback : key;
    }

    public static boolean isAir(final TypedKey<BlockType> key) {
        return key.equals(AIR) || key.equals(CAVE_AIR) || key.equals(VOID_AIR);
    }
}
