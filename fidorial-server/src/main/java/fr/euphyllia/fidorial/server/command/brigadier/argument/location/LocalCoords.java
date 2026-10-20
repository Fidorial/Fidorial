package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.world.CoordMath;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;

public record LocalCoords(LocalCoord x, LocalCoord y, LocalCoord z) {

    public static LocalCoords parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();

        final LocalCoord x = LocalCoord.parse(reader, start);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_3D);
        final LocalCoord y = LocalCoord.parse(reader, start);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_3D);
        final LocalCoord z = LocalCoord.parse(reader, start);

        return new LocalCoords(x, y, z);
    }

    public Location resolve(final CommandSource source) {
        return CoordMath.applyLocalCoords(
                PositionParsing.origin(source),
                x.value(),
                y.value(),
                z.value()
        );
    }
}
