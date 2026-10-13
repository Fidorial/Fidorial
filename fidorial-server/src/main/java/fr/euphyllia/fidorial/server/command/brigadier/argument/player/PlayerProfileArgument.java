package fr.euphyllia.fidorial.server.command.brigadier.argument.player;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntityArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntitySelector;
import fr.euphyllia.fidorial.server.command.brigadier.argument.selector.EntitySelectorParser;
import fr.euphyllia.fidorial.server.command.brigadier.argument.util.ExceptionFactory;
import fr.euphyllia.fidorial.server.command.brigadier.packet.registry.ArgumentTypeRegistrar;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.PlayerProfileMeta;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlayerProfileArgument<T> implements ArgumentType<T> {

    public static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = ExceptionFactory.simple("argument.player.unknown");

    private static final Predicate<Player> ALL = _ -> true;

    private final Predicate<Player> filter;
    private final Function<Result, T> converter;

    private PlayerProfileArgument(final Predicate<Player> filter, final Function<Result, T> converter) {
        this.filter = filter;
        this.converter = converter;
    }

    public static PlayerProfileArgument<Result> playerProfile() {
        return playerProfile(ALL, Function.identity());
    }

    public static <T> PlayerProfileArgument<T> playerProfile(final Function<Result, T> converter) {
        return playerProfile(ALL, converter);
    }

    public static PlayerProfileArgument<Result> playerProfile(final Predicate<Player> filter) {
        return playerProfile(filter, Function.identity());
    }

    public static <T> PlayerProfileArgument<T> playerProfile(final Predicate<Player> filter, final Function<Result, T> converter) {
        return new PlayerProfileArgument<>(filter, converter);
    }

    public static Collection<PlayerProfileMeta> getPlayerProfiles(final CommandContext<CommandSource> context, final String name)
            throws CommandSyntaxException {

        return context.getArgument(name, Result.class).getNames(context.getSource());
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {

        final Result result;

        if (reader.canRead() && reader.peek() == '@') {
            final EntitySelector selector = new EntitySelectorParser(reader).parse();

            if (selector.includesEntities()) {
                throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.create();
            }

            result = new SelectorResult(selector, filter);
        } else {

            final int start = reader.getCursor();

            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }

            final String name = reader.getString().substring(start, reader.getCursor());

            result = source -> {
                final FidorialServer server = (FidorialServer) source.server();

                final Optional<? extends Player> player = server.players().get(name);

                if (player.isEmpty() || !filter.test(player.get())) {
                    throw ERROR_UNKNOWN_PLAYER.create();
                }

                return List.of(new PlayerProfileMeta(player.get().profile()));
            };
        }

        return converter.apply(result);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context,
            final SuggestionsBuilder builder
    ) {
        if (!(context.getSource() instanceof final CommandSource source)) {
            return Suggestions.empty();
        }

        final String remaining = builder.getRemainingLowerCase();

        source.server().players().online().stream()
                .filter(filter)
                .map(Player::name)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    @FunctionalInterface
    public interface Result {

        Collection<PlayerProfileMeta> getNames(CommandSource source) throws CommandSyntaxException;
    }

    public static class SelectorResult implements Result {

        private final EntitySelector selector;
        private final Predicate<Player> filter;

        public SelectorResult(final EntitySelector selector, final Predicate<Player> filter) {
            this.selector = selector;
            this.filter = filter;
        }

        @Override
        public Collection<PlayerProfileMeta> getNames(final CommandSource source) throws CommandSyntaxException {

            final List<Player> players = selector.findPlayers(source).stream()
                    .filter(filter)
                    .toList();

            if (players.isEmpty()) {
                throw EntityArgument.NO_PLAYERS_FOUND.create();
            }

            return players.stream()
                    .map(Player::profile)
                    .map(PlayerProfileMeta::new)
                    .toList();
        }
    }

    public static final class Info implements ArgumentTypeRegistrar<PlayerProfileArgument<?>, Info.Spec> {

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
        public Spec access(final PlayerProfileArgument<?> argument) {
            return new Spec();
        }

        public record Spec() implements ArgumentTypeRegistrar.Spec<PlayerProfileArgument<?>> {

            @Override
            public PlayerProfileArgument<?> instantiate() {
                return PlayerProfileArgument.playerProfile();
            }

            @Override
            public ArgumentTypeRegistrar<PlayerProfileArgument<?>, ?> type() {
                return new Info();
            }
        }
    }
}
