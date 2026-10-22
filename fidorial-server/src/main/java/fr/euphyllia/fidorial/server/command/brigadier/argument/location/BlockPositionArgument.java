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
import fr.fidorial.command.argument.resolvers.BlockPositionResolver;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;

import java.util.concurrent.CompletableFuture;

public final class BlockPositionArgument implements ArgumentType<BlockPositionResolver> {

    public static BlockPositionArgument blockPosition() {
        return new BlockPositionArgument();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context,
            final SuggestionsBuilder builder
    ) {
        return CommonPositionSuggestions.suggest(builder, this, 3);
    }

    @Override
    public BlockPositionResolver parse(final StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '^') {
            final LocalCoords coords = LocalCoords.parse(reader);
            return source -> {
                final Location loc = coords.resolve(source);
                return Position.block(loc.blockX(), loc.blockY(), loc.blockZ());
            };
        }

        final int start = reader.getCursor();
        final BlockCoordinate x = BlockCoordinate.parse(reader);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_3D);
        final BlockCoordinate y = BlockCoordinate.parse(reader);
        PositionParsing.separator(reader, start, PositionParsing.INCOMPLETE_3D);
        final BlockCoordinate z = BlockCoordinate.parse(reader);

        return source -> {
            final Location origin = PositionParsing.origin(source);
            return Position.block(x.resolve(origin.x()), y.resolve(origin.y()), z.resolve(origin.z()));
        };
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
