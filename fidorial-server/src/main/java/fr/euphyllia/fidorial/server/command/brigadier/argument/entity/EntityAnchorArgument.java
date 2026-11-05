package fr.euphyllia.fidorial.server.command.brigadier.argument.entity;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * The vanilla {@code minecraft:entity_anchor} argument: which point of an entity to aim at, its feet or its eyes.
 */
public final class EntityAnchorArgument implements ArgumentType<EntityAnchorArgument.Anchor> {

    public static final DynamicCommandExceptionType ERROR_INVALID = ExceptionFactory.dynamic("argument.anchor.invalid");

    public static EntityAnchorArgument anchor() {
        return new EntityAnchorArgument();
    }

    @Override
    public Anchor parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final String name = reader.readUnquotedString();
        final Anchor anchor = Anchor.byName(name);
        if (anchor == null) {
            reader.setCursor(start);
            throw ERROR_INVALID.createWithContext(reader, name);
        }
        return anchor;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemainingLowerCase();
        for (final Anchor anchor : Anchor.values()) {
            if (anchor.serializedName().startsWith(remaining)) {
                builder.suggest(anchor.serializedName());
            }
        }
        return builder.buildFuture();
    }

    /**
     * A point of an entity.
     */
    public enum Anchor {
        FEET,
        EYES;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        static @Nullable Anchor byName(final String name) {
            for (final Anchor anchor : values()) {
                if (anchor.serializedName().equals(name)) {
                    return anchor;
                }
            }
            return null;
        }
    }

    public static final class Info implements ArgumentTypeRegistrar<EntityAnchorArgument, Info.Spec> {

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
        public Spec access(final EntityAnchorArgument argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<EntityAnchorArgument> {

            @Override
            public EntityAnchorArgument instantiate() {
                return EntityAnchorArgument.anchor();
            }

            @Override
            public ArgumentTypeRegistrar<EntityAnchorArgument, ?> type() {
                return new Info();
            }
        }
    }
}
