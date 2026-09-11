package fr.euphyllia.fidorial.server.world.structure;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public final class Keys {

    public static final Key AIR = Key.key("air");
    public static final Key CAVE_AIR = Key.key("cave_air");
    public static final Key VOID_AIR = Key.key("void_air");
    public static final Key WATER = Key.key("water");
    public static final Key LAVA = Key.key("lava");
    public static final Key JIGSAW = Key.key("jigsaw");
    public static final Key STRUCTURE_VOID = Key.key("structure_void");
    public static final Key STRUCTURE_BLOCK = Key.key("structure_block");
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
        } catch (final InvalidKeyException | IllegalArgumentException invalid) {
            return null;
        }
    }

    public static Key parseOr(final @Nullable String raw, final Key fallback) {
        final Key key = parse(raw);
        return key == null ? fallback : key;
    }

    public static boolean isAir(final Key key) {
        return key.equals(AIR) || key.equals(CAVE_AIR) || key.equals(VOID_AIR);
    }
}
