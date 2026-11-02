package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;

import java.util.concurrent.CompletableFuture;

/**
 * The vanilla {@code minecraft:rotation} argument: a yaw then a pitch, each absolute or relative ({@code ~}) to the
 * rotation of the command source.
 */
public final class RotationArgument implements ArgumentType<RotationArgument.Rotation> {

    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = ExceptionFactory.simple("argument.rotation.incomplete");

    public static RotationArgument rotation() {
        return new RotationArgument();
    }

    @Override
    public Rotation parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        if (!reader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(reader);
        }
        final FineCoordinate yaw = FineCoordinate.parse(reader, false);
        PositionParsing.separator(reader, start, ERROR_NOT_COMPLETE);
        final FineCoordinate pitch = FineCoordinate.parse(reader, false);
        return new Rotation(yaw, pitch);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return CommonPositionSuggestions.suggest(builder, this, 2);
    }

    /**
     * A parsed rotation, resolved against the rotation of a command source.
     */
    public static final class Rotation {

        private final FineCoordinate yaw;
        private final FineCoordinate pitch;

        private Rotation(final FineCoordinate yaw, final FineCoordinate pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        /**
         * {@return the yaw, in degrees, not wrapped}
         */
        public float yaw(final CommandSource source) {
            final Location origin = PositionParsing.origin(source);
            return (float) yaw.resolve(origin.yaw());
        }

        /**
         * {@return the pitch, in degrees, not wrapped}
         */
        public float pitch(final CommandSource source) {
            final Location origin = PositionParsing.origin(source);
            return (float) pitch.resolve(origin.pitch());
        }
    }

    public static final class Info implements ArgumentTypeRegistrar<RotationArgument, Info.Spec> {

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
        public Spec access(final RotationArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<RotationArgument> {

            @Override
            public RotationArgument instantiate() {
                return RotationArgument.rotation();
            }

            @Override
            public ArgumentTypeRegistrar<RotationArgument, ?> type() {
                return new Info();
            }
        }
    }
}
