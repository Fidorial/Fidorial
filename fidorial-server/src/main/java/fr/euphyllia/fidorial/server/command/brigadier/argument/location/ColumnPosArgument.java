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
import fr.fidorial.command.argument.resolvers.ColumnPositionResolver;
import fr.fidorial.math.ColumnPosition;
import fr.fidorial.math.Location;

import java.util.concurrent.CompletableFuture;

public final class ColumnPosArgument implements ArgumentType<ColumnPositionResolver> {

    public static ColumnPosArgument columnPos() {
        return new ColumnPosArgument();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context,
            final SuggestionsBuilder builder
    ) {
        return CommonPositionSuggestions.suggest(builder, this, 2);
    }

    @Override
    public ColumnPositionResolver parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final BlockCoordinate x = BlockCoordinate.parse(reader);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_2D);
        final BlockCoordinate z = BlockCoordinate.parse(reader);

        return source -> {
            final Location origin = PositionParsing.origin(source);
            return ColumnPosition.column(x.resolve(origin.x()), z.resolve(origin.z()));
        };
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
