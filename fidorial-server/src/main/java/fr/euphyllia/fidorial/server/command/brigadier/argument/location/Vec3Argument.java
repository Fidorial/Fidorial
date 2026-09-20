package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;

import java.util.concurrent.CompletableFuture;

public final class Vec3Argument implements ArgumentType<PositionResolver> {

    private final boolean centerCorrect;

    public Vec3Argument(final boolean centerCorrect) {
        this.centerCorrect = centerCorrect;
    }

    public static Vec3Argument vec3() {
        return new Vec3Argument(true);
    }

    public static Position getPosition(final CommandContext<CommandSource> context, final String name) {
        return context.getArgument(name, PositionResolver.class).resolve(context.getSource());
    }

    @Override
    public PositionResolver parse(final StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '^') {
            final LocalCoords coords = LocalCoords.parse(reader);
            return coords::resolve;
        }

        final Coordinate x = Coordinate.parse(reader);
        reader.expect(' ');
        final Coordinate y = Coordinate.parse(reader);
        reader.expect(' ');
        final Coordinate z = Coordinate.parse(reader);

        return source -> {
            final Location origin = source.location();

            double px = x.resolve(origin.x());
            final double py = y.resolve(origin.y());
            double pz = z.resolve(origin.z());

            if (centerCorrect && !x.relative() && !y.relative() && !z.relative()) {
                px += 0.5;
                pz += 0.5;
            }

            return new Location(px, py, pz, origin.yaw(), origin.pitch());
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context,
            final SuggestionsBuilder builder
    ) {
        if (!(context.getSource() instanceof final CommandSource source)
                || !(source.sender() instanceof ServerPlayer)) {
            return Suggestions.empty();
        }

        final Location loc = source.location();

        return CommonPositionSuggestions.suggest(
                builder,
                String.format("%.2f", loc.x()),
                String.format("%.2f", loc.y()),
                String.format("%.2f", loc.z())
        );
    }

    private record Coordinate(double value, boolean relative) {

        static Coordinate parse(final StringReader reader) throws CommandSyntaxException {

            boolean relative = false;

            if (reader.canRead() && reader.peek() == '~') {
                relative = true;
                reader.skip();

                if (!reader.canRead() || reader.peek() == ' ') {
                    return new Coordinate(0, true);
                }
            }

            final double value = reader.readDouble();

            return new Coordinate(value, relative);
        }

        double resolve(final double origin) {
            return relative ? origin + value : value;
        }
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
