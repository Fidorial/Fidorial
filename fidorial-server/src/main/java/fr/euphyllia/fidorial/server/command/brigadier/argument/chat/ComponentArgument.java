package fr.euphyllia.fidorial.server.command.brigadier.argument.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntitySelector;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.EntitySelectorParser;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ComponentArgument implements ArgumentType<Component> {

    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = ExceptionFactory.dynamic("argument.component.invalid");

    private ComponentArgument() {
    }

    public static ComponentArgument textComponent() {
        return new ComponentArgument();
    }

    @Override
    public Component parse(final StringReader reader) throws CommandSyntaxException {
        final String raw = AdventureJsonReader.readRawJson(reader, ERROR_INVALID_COMPONENT);

        try {
            return GsonComponentSerializer.gson().deserialize(raw);
        } catch (final JsonParseException | IllegalArgumentException ex) {
            throw ERROR_INVALID_COMPONENT.createWithContext(reader, raw);
        }
    }

    public static Component getRawComponent(final CommandContext<CommandSource> context, final String name) {
        return context.getArgument(name, Component.class);
    }

    public static Component getResolvedComponent(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        final Component raw = getRawComponent(context, name);
        return resolveSelectors(raw, context.getSource());
    }

    private static Component resolveSelectors(final Component component, final CommandSource source) throws CommandSyntaxException {
        Component result = component;

        if (component instanceof final SelectorComponent selector) {
            final StringReader selectorReader = new StringReader(selector.pattern());
            final EntitySelector parsed = new EntitySelectorParser(selectorReader).parse();
            final Collection<? extends Entity> entities = parsed.findEntities(source);

            final Component separator = selector.separator() != null ? selector.separator() : Component.text(", ");
            Component joined = Component.empty();
            boolean first = true;

            for (final Entity entity : entities) {
                if (!first) {
                    joined = joined.append(separator);
                }
                joined = joined.append(displayNameOf(entity));
                first = false;
            }

            result = joined.style(component.style());
        }

        final List<Component> children = component.children();
        if (!children.isEmpty()) {
            final List<Component> resolvedChildren = new ArrayList<>(children.size());
            for (final Component child : children) {
                resolvedChildren.add(resolveSelectors(child, source));
            }
            result = result.children(resolvedChildren);
        }

        return result;
    }

    private static Component displayNameOf(final Entity entity) {
        if (entity instanceof final Player player) {
            return Component.text(player.name());
        }
        return Component.text(entity.type().key().value());
    }

    public static final class Info implements ArgumentTypeRegistrar<ComponentArgument, Info.Spec> {

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
        public Spec access(final ComponentArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<ComponentArgument> {
            @Override
            public ComponentArgument instantiate() {
                return new ComponentArgument();
            }

            @Override
            public ArgumentTypeRegistrar<ComponentArgument, ?> type() {
                return new Info();
            }
        }
    }
}
