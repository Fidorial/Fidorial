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
import fr.fidorial.command.argument.resolvers.EntitiesResolver;
import fr.fidorial.command.argument.resolvers.EntityResolver;
import fr.fidorial.command.argument.resolvers.PlayerResolver;
import fr.fidorial.command.argument.resolvers.PlayersResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A selector, name or UUID, parsed once and resolved per source by {@code resolver}.
 *
 * @param filter   extra condition every match must satisfy, also applied to suggested names
 * @param resolver turns the parsed selector into what commands read
 */
public record EntityArgument<R>(
        boolean single,
        boolean playersOnly,
        Predicate<Entity> filter,
        Function<EntitySelector, R> resolver
) implements ArgumentType<R> {

    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = ExceptionFactory.simple("argument.entity.toomany");
    public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = ExceptionFactory.simple("argument.player.toomany");
    public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = ExceptionFactory.simple("argument.player.entities");
    public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = ExceptionFactory.simple("argument.entity.notfound.entity");
    public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = ExceptionFactory.simple("argument.entity.notfound.player");
    public static final SimpleCommandExceptionType SELECTORS_NOT_PERMITTED = ExceptionFactory.simple("argument.entity.selector.not_allowed");

    public static EntityArgument<EntityResolver> entity(final Predicate<Entity> filter) {
        return new EntityArgument<>(true, false, filter, selector -> selector::findSingleEntity);
    }

    public static EntityArgument<EntitiesResolver> entities(final Predicate<Entity> filter) {
        return new EntityArgument<>(false, false, filter,
                selector -> source -> nonEmpty(selector.findEntities(source), NO_ENTITIES_FOUND));
    }

    public static EntityArgument<PlayerResolver> player(final Predicate<Player> filter) {
        return new EntityArgument<>(true, true, asEntityFilter(filter), selector -> selector::findSinglePlayer);
    }

    public static EntityArgument<PlayersResolver> players(final Predicate<Player> filter) {
        return new EntityArgument<>(false, true, asEntityFilter(filter),
                selector -> source -> nonEmpty(selector.findPlayers(source), NO_PLAYERS_FOUND));
    }

    private static Predicate<Entity> asEntityFilter(final Predicate<Player> filter) {
        return entity -> entity instanceof final Player player && filter.test(player);
    }

    private static <T> List<T> nonEmpty(final List<T> found, final SimpleCommandExceptionType empty) throws CommandSyntaxException {
        if (found.isEmpty()) {
            throw empty.create();
        }
        return found;
    }

    @Override
    public R parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final EntitySelector selector = new EntitySelectorParser(reader).parse();

        if (single && selector.maxResults() > 1) {
            reader.setCursor(start);
            throw (playersOnly ? ERROR_NOT_SINGLE_PLAYER : ERROR_NOT_SINGLE_ENTITY).createWithContext(reader);
        }
        if (playersOnly && selector.includesEntities() && !selector.selfSelector()) {
            reader.setCursor(start);
            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(reader);
        }
        return resolver.apply(selector.withPredicate(filter));
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
            // suggest from wherever parsing stopped
        }

        return parser.fillSuggestions(builder, names -> {
            final String remaining = names.getRemaining().toLowerCase(Locale.ROOT);
            source.server().players().online().stream()
                    .filter(filter)
                    .map(Player::name)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .forEach(names::suggest);
        });
    }

    public static final class Info implements ArgumentTypeRegistrar<EntityArgument<?>, Info.Spec> {

        private static final int SINGLE = 1;
        private static final int PLAYERS_ONLY = 2;

        @Override
        public void serialize(final Spec spec, final PacketBuffer buf) {
            buf.writeByte((spec.single() ? SINGLE : 0) | (spec.playersOnly() ? PLAYERS_ONLY : 0));
        }

        @Override
        public Spec deserialize(final PacketBuffer buf) {
            final int flags = buf.readByte();
            return new Spec((flags & SINGLE) != 0, (flags & PLAYERS_ONLY) != 0);
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
