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
import fr.fidorial.command.argument.resolvers.BlockPosResolver;
import fr.fidorial.world.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class BlockPositionArgument implements ArgumentType<BlockPosResolver> {

    public static BlockPositionArgument blockPosition() {
        return new BlockPositionArgument();
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

        final int x = (int) Math.floor(loc.x());
        final int y = (int) Math.floor(loc.y());
        final int z = (int) Math.floor(loc.z());

        return CommonPositionSuggestions.suggest(
                builder,
                Integer.toString(x),
                Integer.toString(y),
                Integer.toString(z)
        );
    }

    @Override
    public BlockPosResolver parse(final StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '^') {
            final LocalCoords coords = LocalCoords.parse(reader);
            return source -> {
                final Location loc = coords.resolve(source);

                return new BlockPos(
                        (int) Math.floor(loc.x()),
                        (int) Math.floor(loc.y()),
                        (int) Math.floor(loc.z())
                );
            };
        }

        final Coordinate x = Coordinate.parse(reader);
        reader.expect(' ');
        final Coordinate y = Coordinate.parse(reader);
        reader.expect(' ');
        final Coordinate z = Coordinate.parse(reader);

        return source -> {
            final Location origin = source.location();

            final double px = x.resolve(origin.x());
            final double py = y.resolve(origin.y());
            final double pz = z.resolve(origin.z());

            return new BlockPos(
                    (int) Math.floor(px),
                    (int) Math.floor(py),
                    (int) Math.floor(pz)
            );
        };
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

    public static final class Info implements ArgumentTypeRegistrar<BlockPositionArgument, Info.Spec> {

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
        public Spec access(final BlockPositionArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<BlockPositionArgument> {
            @Override
            public BlockPositionArgument instantiate() {
                return BlockPositionArgument.blockPosition();
            }

            @Override
            public ArgumentTypeRegistrar<BlockPositionArgument, ?> type() {
                return new Info();
            }
        }
    }
}
