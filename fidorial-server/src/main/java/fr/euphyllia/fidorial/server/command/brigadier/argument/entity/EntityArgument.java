package fr.euphyllia.fidorial.server.command.brigadier.argument.entity;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.EntitySelectorParser;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public final class EntityArgument<T> implements ArgumentType<T> {

    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = ExceptionFactory.simple("argument.entity.toomany");
    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = ExceptionFactory.simple("argument.player.toomany");
    public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = ExceptionFactory.simple("argument.player.entities");
    public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = ExceptionFactory.simple("argument.entity.notfound.entity");
    public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = ExceptionFactory.simple("argument.entity.notfound.player");
    public static final SimpleCommandExceptionType SELECTORS_NOT_PERMITTED = ExceptionFactory.simple("argument.entity.selector.not_allowed");

    private final boolean single;
    private final boolean playersOnly;

    private final Predicate<Entity> predicate;
    private final Function<EntitySelector, T> converter;

    public EntityArgument(
            final boolean single,
            final boolean playersOnly,
            final Predicate<Entity> predicate,
            final Function<EntitySelector, T> converter
    ) {
        this.single = single;
        this.playersOnly = playersOnly;
        this.predicate = predicate;
        this.converter = converter;

    }

    // Identity-converter factories: used internally, where the parsed EntitySelector
    // itself is what commands want (see getEntity/getPlayer/... below).

    public static EntityArgument<EntitySelector> entity() {
        return new EntityArgument<>(true, false, _ -> true, Function.identity());
    }

    public static EntityArgument<EntitySelector> entities() {
        return new EntityArgument<>(false, false, _ -> true, Function.identity());
    }

    public static EntityArgument<EntitySelector> player() {
        return new EntityArgument<>(true, true, Player.class::isInstance, Function.identity());
    }

    public static EntityArgument<EntitySelector> players() {
        return new EntityArgument<>(false, true, Player.class::isInstance, Function.identity());
    }

    public boolean single() {
        return single;
    }

    public boolean playersOnly() {
        return playersOnly;
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {

        final int start = reader.getCursor();

        final EntitySelector selector = new EntitySelectorParser(reader).parse();

        if (selector.maxResults() > 1 && single) {

            reader.setCursor(start);

            if (playersOnly) {
                throw ERROR_NOT_SINGLE_PLAYER.createWithContext(reader);
            }

            throw ERROR_NOT_SINGLE_ENTITY.createWithContext(reader);
        }

        if (selector.includesEntities() && playersOnly && !selector.selfSelector()) {

            reader.setCursor(start);

            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(reader);
        }

        return converter.apply(selector.withPredicate(predicate));
    }

    public static Entity getEntity(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {

        return context.getArgument(name, EntitySelector.class).findSingleEntity(context.getSource());
    }

    public static Collection<? extends Entity> getEntities(final CommandContext<CommandSource> context, final String name)
            throws CommandSyntaxException {

        final Collection<? extends Entity> entities = getOptionalEntities(context, name);

        if (entities.isEmpty()) {
            throw NO_ENTITIES_FOUND.create();
        }

        return entities;
    }

    public static Collection<? extends Entity> getOptionalEntities(final CommandContext<CommandSource> context, final String name)
            throws CommandSyntaxException {

        return context.getArgument(name, EntitySelector.class).findEntities(context.getSource());
    }

    public static Player getPlayer(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {

        return context.getArgument(name, EntitySelector.class).findSinglePlayer(context.getSource());
    }

    public static Collection<Player> getOptionalPlayers(final CommandContext<CommandSource> context, final String name)
            throws CommandSyntaxException {

        return context.getArgument(name, EntitySelector.class).findPlayers(context.getSource());
    }

    public static Collection<Player> getPlayers(final CommandContext<CommandSource> context, final String name)
            throws CommandSyntaxException {

        final Collection<Player> players = getOptionalPlayers(context, name);

        if (players.isEmpty()) {
            throw NO_PLAYERS_FOUND.create();
        }

        return players;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof final CommandSource source)) {
            return Suggestions.empty();
        }

        final StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        final EntitySelectorParser parser = new EntitySelectorParser(reader);

        try {
            parser.parse();
        } catch (final CommandSyntaxException _) {
        }

        return parser.fillSuggestions(builder, suggestionsBuilder -> {
            final Collection<String> playerNames = source.server().players().online().stream()
                    .filter(this.predicate)
                    .map(Player::name)
                    .toList();

            final Iterable<String> suggestedNames = this.playersOnly
                    ? playerNames
                    : playerNames; // extend here in the future

            for (final String name : suggestedNames) {
                if (name.toLowerCase(Locale.ROOT).startsWith(suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT))) {
                    suggestionsBuilder.suggest(name);
                }
            }
        });
    }

    public static final class Info implements ArgumentTypeRegistrar<EntityArgument<?>, Info.Spec> {

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
            int flags = 0;

            if (spec.single()) flags |= 1;

            if (spec.playersOnly()) flags |= 2;

            buf.writeByte(flags);
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            final int flags = buf.readByte();
            return new Spec((flags & 1) != 0, (flags & 2) != 0);
        }

        @Override
        public void serializeJson(final Spec spec, final JsonObject json) {
            json.addProperty("single", spec.single());
            json.addProperty("players_only", spec.playersOnly());
        }

        @Override
        public Spec access(final EntityArgument<?> argument) {
            return new Spec(argument.single(), argument.playersOnly());
        }

        public record Spec(boolean single, boolean playersOnly) implements ArgumentTypeRegistrar.Spec<EntityArgument<?>> {

            @Override
            public EntityArgument<?> instantiate() {
                return new EntityArgument<>(single, playersOnly, _ -> true, Function.identity());
            }

            @Override
            public ArgumentTypeRegistrar<EntityArgument<?>, ?> type() {
                return new Info();
            }
        }
    }
}
