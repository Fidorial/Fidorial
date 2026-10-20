package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

record FineCoordinate(double value, boolean relative) {

    /**
     * @param center whether an absolute whole number is moved to the block center
     */
    static FineCoordinate parse(final StringReader reader, final boolean center) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw PositionParsing.MISSING_DOUBLE.createWithContext(reader);
        }
        if (reader.peek() == '^') {
            throw PositionParsing.MIXED.createWithContext(reader);
        }

        final boolean relative = reader.peek() == '~';
        if (relative) {
            reader.skip();
            if (!reader.canRead() || reader.peek() == ' ') {
                return new FineCoordinate(0, true);
            }
        }

        final int start = reader.getCursor();
        final double value = reader.readDouble();
        final boolean whole = reader.getString().substring(start, reader.getCursor()).indexOf('.') < 0;
        return new FineCoordinate(center && !relative && whole ? value + 0.5 : value, relative);
    }

    double resolve(final double origin) {
        return relative ? origin + value : value;
    }
}
