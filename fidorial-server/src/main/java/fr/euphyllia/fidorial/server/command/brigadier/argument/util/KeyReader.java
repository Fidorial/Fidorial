package fr.euphyllia.fidorial.server.command.brigadier.argument.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.key.Key;

public final class KeyReader {

    public static final SimpleCommandExceptionType INVALID_KEY = ExceptionFactory.simple("argument.id.invalid");

    private KeyReader() {
    }

    public static Key readKey(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final String raw = readKeyString(reader);
        if (!Key.parseable(raw)) {
            reader.setCursor(start);
            throw INVALID_KEY.createWithContext(reader);
        }
        return Key.key(raw);
    }

    private static String readKeyString(final StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowed(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private static boolean isAllowed(final char c) {
        return c == Key.DEFAULT_SEPARATOR || Key.allowedInValue(c);
    }
}
