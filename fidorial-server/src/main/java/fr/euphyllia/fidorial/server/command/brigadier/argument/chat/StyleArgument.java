package fr.euphyllia.fidorial.server.command.brigadier.argument.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.Set;

public final class StyleArgument implements ArgumentType<Style> {

    private static final Set<String> CONTENT_KEYS = Set.of("text", "translate", "selector", "score", "keybind", "nbt");

    public static final DynamicCommandExceptionType ERROR_INVALID_STYLE = ExceptionFactory.dynamic("argument.style.invalid");

    private StyleArgument() {
    }

    public static StyleArgument style() {
        return new StyleArgument();
    }

    @Override
    public Style parse(final StringReader reader) throws CommandSyntaxException {
        final String raw = AdventureJsonReader.readRawJson(reader, ERROR_INVALID_STYLE);

        final JsonElement element;
        try {
            element = JsonParser.parseString(raw);
        } catch (final JsonParseException ex) {
            throw ERROR_INVALID_STYLE.createWithContext(reader, raw);
        }

        if (!(element instanceof final JsonObject object)) {
            throw ERROR_INVALID_STYLE.createWithContext(reader, raw);
        }

        final boolean hasContent = CONTENT_KEYS.stream().anyMatch(object::has);
        if (!hasContent) {
            object.addProperty("text", "");
        }

        try {
            final Component component = GsonComponentSerializer.gson().deserializeFromTree(object);
            return component.style();
        } catch (final JsonParseException | IllegalArgumentException ex) {
            throw ERROR_INVALID_STYLE.createWithContext(reader, raw);
        }
    }

    public static Style getStyle(final CommandContext<CommandSource> context, final String name) {
        return context.getArgument(name, Style.class);
    }

    public static final class Info implements ArgumentTypeRegistrar<StyleArgument, Info.Spec> {

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
        public Spec access(final StyleArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<StyleArgument> {
            @Override
            public StyleArgument instantiate() {
                return new StyleArgument();
            }

            @Override
            public ArgumentTypeRegistrar<StyleArgument, ?> type() {
                return new Info();
            }
        }
    }
}
