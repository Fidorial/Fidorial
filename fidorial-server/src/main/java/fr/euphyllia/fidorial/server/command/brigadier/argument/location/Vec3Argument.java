package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.math.Location;

import java.util.concurrent.CompletableFuture;

public final class Vec3Argument implements ArgumentType<PositionResolver> {

    private final boolean centerCorrect;

    public Vec3Argument(final boolean centerCorrect) {
        this.centerCorrect = centerCorrect;
    }

    public static Vec3Argument vec3() {
        return new Vec3Argument(true);
    }

    @Override
    public PositionResolver parse(final StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '^') {
            final LocalCoords coords = LocalCoords.parse(reader);
            return coords::resolve;
        }

        final int start = reader.getCursor();
        final FineCoordinate x = FineCoordinate.parse(reader, centerCorrect);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_3D);
        final FineCoordinate y = FineCoordinate.parse(reader, false);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_3D);
        final FineCoordinate z = FineCoordinate.parse(reader, centerCorrect);

        return source -> {
            final Location origin = PositionParsing.origin(source);
            return origin.with(x.resolve(origin.x()), y.resolve(origin.y()), z.resolve(origin.z()));
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context,
            final SuggestionsBuilder builder
    ) {
        return CommonPositionSuggestions.suggest(builder, this, 3);
    }

    public boolean centerCorrect() {
        return centerCorrect;
    }

    public static final class Info implements ArgumentTypeRegistrar<Vec3Argument, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            return new Spec();
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
        }

        @Override
        public Spec access(final Vec3Argument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<Vec3Argument> {

            @Override
            public Vec3Argument instantiate() {
                return Vec3Argument.vec3();
            }

            @Override
            public ArgumentTypeRegistrar<Vec3Argument, ?> type() {
                return new Info();
            }
        }
    }
}
