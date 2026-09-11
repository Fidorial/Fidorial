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
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;

import java.util.concurrent.CompletableFuture;

public final class ColumnPosArgument implements ArgumentType<ColumnPosArgument.Resolver> {

    public static ColumnPosArgument columnPos() {
        return new ColumnPosArgument();
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
        final String x = Integer.toString((int) Math.floor(loc.x()));
        final String z = Integer.toString((int) Math.floor(loc.z()));

        final String remaining = builder.getRemaining();
        if (remaining.isEmpty()) {
            builder.suggest(x);
            builder.suggest(x + " " + z);
            builder.suggest("~");
            builder.suggest("~ ~");
        } else if (!remaining.contains(" ")) {
            builder.suggest(remaining + " " + z);
        }

        return builder.buildFuture();
    }

    @Override
    public Resolver parse(final StringReader reader) throws CommandSyntaxException {
        final Coordinate x = Coordinate.parse(reader);
        reader.expect(' ');
        final Coordinate z = Coordinate.parse(reader);

        return source -> {
            final Location origin = source.location();
            return new Column(x.resolve(origin.x()), z.resolve(origin.z()));
        };
    }

    /**
     * Resolves the parsed column against the location of a {@link CommandSource}.
     */
    @FunctionalInterface
    public interface Resolver {
        Column resolve(CommandSource source);
    }

    /**
     * A block column, without height.
     */
    public record Column(int x, int z) {

        public ChunkPos chunk() {
            return ChunkPos.fromBlock(x, z);
        }
    }

    private record Coordinate(double value, boolean relative) {

        static Coordinate parse(final StringReader reader) throws CommandSyntaxException {
            if (reader.canRead() && reader.peek() == '~') {
                reader.skip();

                if (!reader.canRead() || reader.peek() == ' ') {
                    return new Coordinate(0, true);
                }
                return new Coordinate(reader.readDouble(), true);
            }

            return new Coordinate(reader.readInt(), false);
        }

        int resolve(final double origin) {
            return (int) Math.floor(relative ? origin + value : value);
        }
    }

    public static final class Info implements ArgumentTypeRegistrar<ColumnPosArgument, Info.Spec> {

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
        public Spec access(final ColumnPosArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<ColumnPosArgument> {
            @Override
            public ColumnPosArgument instantiate() {
                return ColumnPosArgument.columnPos();
            }

            @Override
            public ArgumentTypeRegistrar<ColumnPosArgument, ?> type() {
                return new Info();
            }
        }
    }
}
