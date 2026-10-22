package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;

import java.util.Objects;

final class PositionParsing {

    static final SimpleCommandExceptionType MIXED = ExceptionFactory.simple("argument.pos.mixed");
    static final SimpleCommandExceptionType INCOMPLETE_2D = ExceptionFactory.simple("argument.pos2d.incomplete");
    static final SimpleCommandExceptionType INCOMPLETE_3D = ExceptionFactory.simple("argument.pos3d.incomplete");
    static final SimpleCommandExceptionType MISSING_DOUBLE = ExceptionFactory.simple("argument.pos.missing.double");
    static final SimpleCommandExceptionType MISSING_INT = ExceptionFactory.simple("argument.pos.missing.int");

    private PositionParsing() {
    }

    static void separator(final StringReader reader, final int start, final SimpleCommandExceptionType incomplete) throws CommandSyntaxException {
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(start);
            throw incomplete.createWithContext(reader);
        }
        reader.skip();
    }

    static Location origin(final CommandSource source) {
        return Objects.requireNonNull(source.location(),
                "Resolving a position needs a source with a location; use CommandSource#at");
    }
}
