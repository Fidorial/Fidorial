package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.world.CoordMath;
import fr.fidorial.annotation.SinceMinecraft;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;

/**
 * Two-dimensional local coordinates, {@code ^left ^forwards}, accepted by the column position
 * arguments. Only the yaw of the source matters.
 */
@SinceMinecraft("26.4-snapshot-9")
public record LocalColumnCoords(LocalCoord x, LocalCoord z) {

    public static LocalColumnCoords parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();

        final LocalCoord x = LocalCoord.parse(reader, start);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_2D);
        final LocalCoord z = LocalCoord.parse(reader, start);

        return new LocalColumnCoords(x, z);
    }

    public Location resolve(final CommandSource source) {
        return CoordMath.applyLocalHorizontalCoords(PositionParsing.origin(source), x.value(), z.value());
    }
}
