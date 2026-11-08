package fr.euphyllia.fidorial.server.command.brigadier.argument.entity;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.command.CommandSource;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UuidArgument implements ArgumentType<UUID> {

    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^([-A-Fa-f0-9]+)");

    private static final SimpleCommandExceptionType ERROR_INVALID_UUID = ExceptionFactory.simple("argument.uuid.invalid");

    public static UuidArgument uuid() {
        return new UuidArgument();
    }

    public static UUID getUuid(final CommandContext<CommandSource> context, final String name) {
        return context.getArgument(name, UUID.class);
    }

    public UuidArgument() {
    }

    @Override
    public UUID parse(final StringReader reader) throws CommandSyntaxException {

        final String remaining = reader.getRemaining();
        final Matcher matcher = ALLOWED_CHARACTERS.matcher(remaining);

        final String value = matcher.find() ? matcher.group(1) : null;
        if (value != null) {
            try {
                final UUID uuid = UUID.fromString(value);
                reader.setCursor(reader.getCursor() + value.length());
                return uuid;
            } catch (IllegalArgumentException _) {
            }
        }

        throw ERROR_INVALID_UUID.createWithContext(reader);
    }

    public static final class Info implements ArgumentTypeRegistrar<UuidArgument, Info.Spec> {

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
        public Spec access(final UuidArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<UuidArgument> {

            @Override
            public UuidArgument instantiate() {
                return new UuidArgument();
            }

            @Override
            public ArgumentTypeRegistrar<UuidArgument, ?> type() {
                return new Info();
            }
        }
    }
}
