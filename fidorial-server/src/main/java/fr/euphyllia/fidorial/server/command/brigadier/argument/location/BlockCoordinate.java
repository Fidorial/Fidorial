package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

record BlockCoordinate(double value, boolean relative) {

    static BlockCoordinate parse(final StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw PositionParsing.MISSING_INT.createWithContext(reader);
        }
        if (reader.peek() == '^') {
            throw PositionParsing.MIXED.createWithContext(reader);
        }
        if (reader.peek() == '~') {
            reader.skip();
            if (!reader.canRead() || reader.peek() == ' ') {
                return new BlockCoordinate(0, true);
            }
            return new BlockCoordinate(reader.readDouble(), true);
        }
        return new BlockCoordinate(reader.readInt(), false);
    }

    int resolve(final double origin) {
        return (int) Math.floor(relative ? origin + value : value);
    }
}
