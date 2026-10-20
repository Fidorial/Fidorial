package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public record LocalCoord(double value) {

    static LocalCoord parse(final StringReader reader, final int start) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw PositionParsing.MISSING_DOUBLE.createWithContext(reader);
        }
        if (reader.peek() != '^') {
            reader.setCursor(start);
            throw PositionParsing.MIXED.createWithContext(reader);
        }
        reader.skip();

        if (!reader.canRead() || reader.peek() == ' ') {
            return new LocalCoord(0);
        }

        return new LocalCoord(reader.readDouble());
    }
}
