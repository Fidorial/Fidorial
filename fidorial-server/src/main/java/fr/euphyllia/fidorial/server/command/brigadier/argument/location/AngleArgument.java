package fr.euphyllia.fidorial.server.command.brigadier.argument.location;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.argument.resolvers.AngleResolver;

public final class AngleArgument implements ArgumentType<AngleResolver> {

    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = ExceptionFactory.simple("argument.angle.incomplete");
    public static final SimpleCommandExceptionType ERROR_INVALID_ANGLE = ExceptionFactory.simple("argument.angle.invalid");

    public static AngleArgument angle() {
        return new AngleArgument();
    }

    @Override
    public AngleResolver parse(final StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext(reader);
        }

        final boolean relative = reader.peek() == '~';
        if (relative) {
            reader.skip();
        }

        final float value = reader.canRead() && reader.peek() != ' ' ? reader.readFloat() : 0.0F;

        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw ERROR_INVALID_ANGLE.createWithContext(reader);
        }

        return source -> {
            final float baseYaw = relative ? PositionParsing.origin(source).yaw() : 0.0F;
            float result = (relative ? baseYaw + value : value) % 360.0F;
            if (result >= 180.0F) {
                result -= 360.0F;
            }
            if (result < -180.0F) {
                result += 360.0F;
            }
            return result;
        };
    }

    public static final class Info implements ArgumentTypeRegistrar<AngleArgument, Info.Spec> {

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
        public Spec access(final AngleArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<AngleArgument> {
            @Override
            public AngleArgument instantiate() {
                return AngleArgument.angle();
            }

            @Override
            public ArgumentTypeRegistrar<AngleArgument, ?> type() {
                return new Info();
            }
        }
    }
}
