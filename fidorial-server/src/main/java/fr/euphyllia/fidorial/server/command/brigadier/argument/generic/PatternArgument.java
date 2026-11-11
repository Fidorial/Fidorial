package fr.euphyllia.fidorial.server.command.brigadier.argument.generic;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import io.netty.handler.codec.EncoderException;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the input matching {@code pattern} from the cursor; the match may stop before the end of the input.
 *
 * @param pattern      the pattern, without flags since only its source is sent to clients
 * @param errorMessage shown instead of the default error when the input doesn't match, or {@code null}
 */
public record PatternArgument(Pattern pattern, @Nullable Component errorMessage) implements ArgumentType<String> {

    private static final SimpleCommandExceptionType ERROR_NOT_MATCHED = ExceptionFactory.simple("argument.pattern.not_matched");

    public PatternArgument {
        Preconditions.checkArgument(pattern.flags() == 0,
                "Pattern flags aren't sent to clients, use inline flags instead: %s", pattern);
    }

    public static PatternArgument pattern(final Pattern pattern, final @Nullable Component errorMessage) {
        return new PatternArgument(pattern, errorMessage);
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        final String input = reader.getString();
        final Matcher matcher = pattern.matcher(input)
                .region(reader.getCursor(), input.length());

        if (!matcher.lookingAt()) {
            throw notMatched(reader);
        }

        final int end = matcher.end();
        reader.setCursor(end);
        return input.substring(matcher.start(), end);
    }

    private CommandSyntaxException notMatched(final StringReader reader) {
        final Component message = errorMessage;
        if (message != null) {
            return new CommandSyntaxException(ERROR_NOT_MATCHED, Message.component(message), reader.getString(), reader.getCursor());
        }
        return ERROR_NOT_MATCHED.createWithContext(reader);
    }

    public static final class Info implements ArgumentTypeRegistrar<PatternArgument, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
            buf.writeString(spec.pattern().pattern());
            final Component message = spec.errorMessage();
            buf.writeBoolean(message != null);
            if (message != null) {
                buf.writeComponent(message);
            }
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            final Pattern pattern = Pattern.compile(buf.readString(StringLikeCodecs.MAX_STRING_LENGTH));
            final Component message = buf.readBoolean() ? buf.readComponent(TextCodecs.MAX_COMPONENT_BYTES) : null;
            return new Spec(pattern, message);
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
            json.addProperty("pattern", spec.pattern().pattern());
            final Component message = spec.errorMessage();
            if (message != null) {
                final JsonElement parsed = ComponentCodecs.COMPONENT_CODEC.encodeStart(JsonOps.INSTANCE, message)
                        .getOrThrow(msg -> new EncoderException("Failed to encode Component: " + msg));
                json.add("error_message", parsed);
            }
        }

        @Override
        public Spec access(final PatternArgument argument) {
            return new Spec(argument.pattern(), argument.errorMessage());
        }

        public record Spec(Pattern pattern, @Nullable Component errorMessage) implements ArgumentTypeRegistrar.Spec<PatternArgument> {

            @Override
            public PatternArgument instantiate() {
                return new PatternArgument(pattern, errorMessage);
            }

            @Override
            public ArgumentTypeRegistrar<PatternArgument, ?> type() {
                return new Info();
            }
        }
    }
}
