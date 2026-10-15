package fr.fidorial.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.custom.ArgumentMapper;
import fr.fidorial.command.argument.predicate.ItemStackPredicate;
import fr.fidorial.command.argument.range.DoubleRangeProvider;
import fr.fidorial.command.argument.range.IntegerRangeProvider;
import fr.fidorial.command.argument.resolvers.AngleResolver;
import fr.fidorial.command.argument.resolvers.BlockPosResolver;
import fr.fidorial.command.argument.resolvers.NbtPathResolver;
import fr.fidorial.command.argument.resolvers.PlayerProfileListResolver;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.command.argument.resolvers.selector.EntitySelectorArgumentResolver;
import fr.fidorial.command.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.World;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The argument types the server knows how to parse, suggest and send to clients.
 *
 * <pre>{@code argument("targets", Arguments.arguments().players())}</pre>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface Arguments {

    /**
     * Gets the available argument types.
     * @return the argument types
     * @since 0.1.0
     */
    static Arguments arguments() {
        final class Holder {
            static final Arguments INSTANCE = ServiceLoader.load(Arguments.class, Arguments.class.getClassLoader())
                    .findFirst()
                    .orElseThrow();
        }
        return Holder.INSTANCE;
    }

    /**
     * {@return a selector matching exactly one entity}
     *
     * @since 0.1.0
     */
    ArgumentType<EntitySelectorArgumentResolver> entity();

    /**
     * {@return a selector matching exactly one entity accepted by {@code filter}}
     *
     * @param filter the predicate the entity must satisfy
     * @since 0.1.0
     */
    ArgumentType<EntitySelectorArgumentResolver> entity(Predicate<Entity> filter);

    /**
     * {@return a selector matching any number of entities}
     *
     * @since 0.1.0
     */
    ArgumentType<EntitySelectorArgumentResolver> entities();

    /**
     * {@return a selector matching any number of entities accepted by {@code filter}}
     *
     * @param filter the predicate each entity must satisfy
     * @since 0.1.0
     */
    ArgumentType<EntitySelectorArgumentResolver> entities(Predicate<Entity> filter);

    /**
     * {@return a selector matching exactly one player}
     *
     * @since 0.1.0
     */
    ArgumentType<PlayerSelectorArgumentResolver> player();

    /**
     * {@return a selector matching exactly one player accepted by {@code filter}}
     *
     * @param filter the predicate the player must satisfy
     * @since 0.1.0
     */
    ArgumentType<PlayerSelectorArgumentResolver> player(Predicate<Player> filter);

    /**
     * {@return a selector matching any number of players}
     *
     * @since 0.1.0
     */
    ArgumentType<PlayerSelectorArgumentResolver> players();

    /**
     * {@return a selector matching any number of players accepted by {@code filter}}
     *
     * @param filter the predicate each player must satisfy
     * @since 0.1.0
     */
    ArgumentType<PlayerSelectorArgumentResolver> players(Predicate<Player> filter);

    /**
     * {@return a selector or name list resolving to player profiles}
     *
     * @since 0.1.0
     */
    ArgumentType<PlayerProfileListResolver> playerProfiles();

    /**
     * {@return a selector or name list resolving to player profiles, online players filtered by {@code filter}}
     *
     * @param filter the predicate each online player must satisfy
     * @since 0.1.0
     */
    ArgumentType<PlayerProfileListResolver> playerProfiles(Predicate<Player> filter);

    /**
     * {@return a block position, absolute, relative ({@code ~}) or local ({@code ^})}
     *
     * @since 0.1.0
     */
    ArgumentType<BlockPosResolver> blockPosition();

    /**
     * {@return a 3D position, absolute, relative ({@code ~}) or local ({@code ^})}
     *
     * @since 0.1.0
     */
    ArgumentType<PositionResolver> position();

    /**
     * {@return an angle in degrees}
     *
     * @since 0.1.0
     */
    ArgumentType<AngleResolver> angle();

    /**
     * {@return an item stack, with its components}
     *
     * @since 0.1.0
     */
    ArgumentType<ItemStack> itemStack();

    /**
     * {@return an item predicate}
     *
     * @since 0.1.0
     */
    ArgumentType<ItemStackPredicate> itemPredicate();

    /**
     * {@return a {@link NamedTextColor}}
     *
     * @since 0.1.0
     */
    ArgumentType<NamedTextColor> namedColor();

    /**
     * {@return a hex color}
     *
     * @since 0.1.0
     */
    ArgumentType<TextColor> hexColor();

    /**
     * {@return a boss bar color}
     *
     * @since 0.1.0
     */
    ArgumentType<BossBar.Color> bossBarColor();

    /**
     * {@return a boss bar overlay}
     *
     * @since 0.1.0
     */
    ArgumentType<BossBar.Overlay> bossBarOverlay();

    /**
     * {@return a boss bar flag}
     *
     * @since 0.1.0
     */
    ArgumentType<BossBar.Flag> bossBarFlag();

    /**
     * {@return a text component}
     *
     * @since 0.1.0
     */
    ArgumentType<Component> component();

    /**
     * {@return a style}
     *
     * @since 0.1.0
     */
    ArgumentType<Style> style();

    /**
     * {@return a key}
     *
     * @since 0.1.0
     */
    ArgumentType<Key> key();

    /**
     * {@return a single unquoted word}
     *
     * @since 0.1.0
     */
    ArgumentType<String> word();

    /**
     * {@return a single word or a quoted phrase}
     *
     * @since 0.1.0
     */
    ArgumentType<String> string();

    /**
     * {@return the remainder of the input}
     *
     * @since 0.1.0
     */
    ArgumentType<String> greedyString();

    /**
     * {@return a boolean}
     *
     * @since 0.1.0
     */
    ArgumentType<Boolean> bool();

    /**
     * {@return an unbounded integer}
     *
     * @since 0.1.0
     */
    default ArgumentType<Integer> integer() {
        return integer(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * {@return an integer within {@code [min, max]}}
     *
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @since 0.1.0
     */
    ArgumentType<Integer> integer(int min, int max);

    /**
     * {@return an unbounded long}
     *
     * @since 0.1.0
     */
    default ArgumentType<Long> longArg() {
        return longArg(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * {@return a long within {@code [min, max]}}
     *
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @since 0.1.0
     */
    ArgumentType<Long> longArg(long min, long max);

    /**
     * {@return an unbounded float}
     *
     * @since 0.1.0
     */
    default ArgumentType<Float> floatArg() {
        return floatArg(-Float.MAX_VALUE, Float.MAX_VALUE);
    }

    /**
     * {@return a float within {@code [min, max]}}
     *
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @since 0.1.0
     */
    ArgumentType<Float> floatArg(float min, float max);

    /**
     * {@return an unbounded double}
     *
     * @since 0.1.0
     */
    default ArgumentType<Double> doubleArg() {
        return doubleArg(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    /**
     * {@return a double within {@code [min, max]}}
     *
     * @param min the minimum, inclusive
     * @param max the maximum, inclusive
     * @since 0.1.0
     */
    ArgumentType<Double> doubleArg(double min, double max);

    /**
     * {@return an inclusive integer range, possibly open on either end}
     *
     * @since 0.1.0
     */
    ArgumentType<IntegerRangeProvider> integerRange();

    /**
     * {@return an inclusive double range, possibly open on either end}
     *
     * @since 0.1.0
     */
    ArgumentType<DoubleRangeProvider> doubleRange();

    /**
     * {@return a loaded world}
     *
     * @since 0.1.0
     */
    ArgumentType<World> world();

    /**
     * {@return a game mode}
     *
     * @since 0.1.0
     */
    ArgumentType<GameMode> gameMode();

    /**
     * {@return a UUID}
     *
     * @since 0.1.0
     */
    ArgumentType<UUID> uuid();

    /**
     * {@return a time in ticks, such as {@code 1d}, {@code 5s}, {@code 6t} or {@code 2}}
     *
     * @since 0.1.0
     */
    default ArgumentType<Integer> time() {
        return time(0);
    }

    /**
     * {@return a time in ticks of at least {@code minTicks}}
     *
     * @param minTicks the minimum, inclusive
     * @since 0.1.0
     */
    ArgumentType<Integer> time(int minTicks);

    /**
     * {@return a chain of amount/unit pairs ({@code w}, {@code d}, {@code h}, {@code m}, {@code s}), such as {@code 1w3d12h}}
     *
     * <p>A bare number is rejected.</p>
     *
     * @since 0.1.0
     */
    ArgumentType<Duration> duration();

    /**
     * {@return a typed key of {@code registryKey}, not checked against the registry}
     *
     * @param registryKey the registry
     * @param <T>         the registry value type
     * @since 0.1.0
     */
    <T> ArgumentType<TypedKey<T>> resourceKey(RegistryKey<T> registryKey);

    /**
     * {@return a value of {@code registryKey}, resolved when parsed; suggested by the client}
     *
     * @param registryKey the registry
     * @param <T>         the registry value type
     * @since 0.1.0
     */
    <T> ArgumentType<T> resource(RegistryKey<T> registryKey);

    /**
     * {@return a value of {@code registryKey}, resolved when parsed; validated and suggested by the server}
     *
     * @param registryKey the registry
     * @param <T>         the registry value type
     * @apiNote Sent to clients as {@link #resourceKey(RegistryKey)}, for {@link Registry registries} clients don't know.
     * @since 0.1.0
     */
    <T> ArgumentType<T> serverResource(RegistryKey<T> registryKey);

    /**
     * {@return an NBT path, resolvable against any root {@link BinaryTag}}
     *
     * @since 0.1.0
     */
    ArgumentType<NbtPathResolver> nbtPath();

    /**
     * {@return a swing animation type}
     *
     * @since 0.1.0
     */
    ArgumentType<SwingAnimation.SwingAnimationType> swingAnimationType();

    /**
     * {@return a post effect; players are suggested those of their loaded resource packs when possible}
     *
     * @since 0.1.0
     */
    ArgumentType<Key> postEffect();

    /**
     * {@return {@code nativeType}, its parsed value converted by {@code mapper}}
     *
     * <p>Keeps the native type's client-side grammar, highlighting and suggestions.</p>
     *
     * @param nativeType the type providing grammar and suggestions
     * @param mapper     converts the parsed value
     * @param <N>        the native value type
     * @param <T>        the mapped value type
     * @since 0.1.0
     */
    <N, T> ArgumentType<T> map(ArgumentType<N> nativeType, ArgumentMapper<N, T> mapper);

    /**
     * {@return {@code nativeType}, its parsed value converted by {@code mapper} and its suggestions replaced}
     *
     * <p>For when the native grammar fits but its completions don't, like a {@link #word()} mapped to an enum.</p>
     *
     * @param nativeType  the type providing grammar
     * @param mapper      converts the parsed value
     * @param suggestions suggestions computed by the server
     * @param <N>         the native value type
     * @param <T>         the mapped value type
     * @since 0.1.0
     */
    <N, T> ArgumentType<T> map(ArgumentType<N> nativeType, ArgumentMapper<N, T> mapper, SuggestionProvider<CommandSource> suggestions);

    /**
     * {@return {@code type}, suggested by the server instead of the client}
     *
     * <p>For hand-rolled {@code parse}/{@code listSuggestions} logic.</p>
     *
     * @param type the argument type
     * @param <T>  the parsed value type
     * @since 0.1.0
     */
    <T> ArgumentType<T> withServerSuggestions(ArgumentType<T> type);

    /**
     * {@return {@code type}, suggested by the client from {@code suggestionSource}}
     *
     * <p>When the server has to answer (console, client unaware of the source), it uses
     * {@code fallbackSuggestions}, or {@code type}'s own if {@code null}.</p>
     *
     * @param type                the argument type
     * @param suggestionSource    the client-side suggestion source
     * @param fallbackSuggestions server-side suggestions, or {@code null}
     * @param <T>                 the parsed value type
     * @since 0.1.0
     */
    <T> ArgumentType<T> withClientSuggestions(ArgumentType<T> type, Key suggestionSource, @Nullable SuggestionProvider<CommandSource> fallbackSuggestions);
}
