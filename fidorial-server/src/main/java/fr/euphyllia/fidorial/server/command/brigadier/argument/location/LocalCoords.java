package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.euphyllia.fidorial.server.world.CoordMath;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Position;

public record LocalCoords(LocalCoord x, LocalCoord y, LocalCoord z) {

    public static LocalCoords parse(final StringReader reader)
            throws CommandSyntaxException {

        final LocalCoord x = LocalCoord.parse(reader);
        reader.expect(' ');
        final LocalCoord y = LocalCoord.parse(reader);
        reader.expect(' ');
        final LocalCoord z = LocalCoord.parse(reader);

        return new LocalCoords(x, y, z);
    }

    public Position resolve(final CommandSource source) {
        return CoordMath.applyLocalCoords(
                source.location(),
                x.value(),
                y.value(),
                z.value()
        );
    }
}
