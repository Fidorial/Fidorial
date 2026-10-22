package fr.euphyllia.fidorial.server.command.brigadier.argument;

import com.google.common.collect.Range;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.brigadier.argument.bossbar.BossBarColorArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.bossbar.BossBarFlagArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.bossbar.BossBarOverlayArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.chat.ComponentArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.chat.HexColorArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.chat.NamedColorArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.chat.StyleArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.custom.ClientSuggestionsArgumentType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.custom.MappedArgumentType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.custom.ServerSuggestionsArgumentType;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.EntityArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.entity.UuidArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.generic.DurationArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.generic.TimeArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.item.ItemArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.item.ItemPredicateArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.item.component.SwingAnimationTypeArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.AngleArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.BlockPositionArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.ColumnPosArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.DimensionArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.location.Vec3Argument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.nbt.NbtDataArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.player.GameModeArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.player.PlayerProfileArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.player.PostEffectArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.range.RangeArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.range.RangeBounds;
import fr.euphyllia.fidorial.server.command.brigadier.argument.resource.KeyArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.resource.ResourceArgument;
import fr.euphyllia.fidorial.server.command.brigadier.argument.resource.ResourceKeyArgument;
import fr.fidorial.command.CommandSource;
import fr.fidorial.command.argument.Arguments;
import fr.fidorial.command.argument.custom.ArgumentMapper;
import fr.fidorial.command.argument.resolvers.AngleResolver;
import fr.fidorial.command.argument.resolvers.BlockPositionResolver;
import fr.fidorial.command.argument.resolvers.ColumnPositionResolver;
import fr.fidorial.command.argument.resolvers.EntitiesResolver;
import fr.fidorial.command.argument.resolvers.EntityResolver;
import fr.fidorial.command.argument.resolvers.PlayerProfilesResolver;
import fr.fidorial.command.argument.resolvers.PlayerResolver;
import fr.fidorial.command.argument.resolvers.PlayersResolver;
import fr.fidorial.command.argument.resolvers.PositionResolver;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.nbt.NbtPath;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.World;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class FidorialArguments implements Arguments {

    private static final Predicate<Player> ALL_PLAYERS = _ -> true;

    @Override
    public ArgumentType<EntityResolver> entity() {
        return EntityArgument.entity(_ -> true);
    }

    @Override
    public ArgumentType<EntityResolver> entity(final Predicate<Entity> filter) {
        return withServerSuggestions(EntityArgument.entity(filter));
    }

    @Override
    public ArgumentType<EntitiesResolver> entities() {
        return EntityArgument.entities(_ -> true);
    }

    @Override
    public ArgumentType<EntitiesResolver> entities(final Predicate<Entity> filter) {
        return withServerSuggestions(EntityArgument.entities(filter));
    }

    @Override
    public ArgumentType<PlayerResolver> player() {
        return EntityArgument.player(ALL_PLAYERS);
    }

    @Override
    public ArgumentType<PlayerResolver> player(final Predicate<Player> filter) {
        return withServerSuggestions(EntityArgument.player(filter));
    }

    @Override
    public ArgumentType<PlayersResolver> players() {
        return EntityArgument.players(ALL_PLAYERS);
    }

    @Override
    public ArgumentType<PlayersResolver> players(final Predicate<Player> filter) {
        return withServerSuggestions(EntityArgument.players(filter));
    }

    @Override
    public ArgumentType<PlayerProfilesResolver> playerProfiles() {
        return profiles(ALL_PLAYERS);
    }

    @Override
    public ArgumentType<PlayerProfilesResolver> playerProfiles(final Predicate<Player> filter) {
        return withServerSuggestions(profiles(filter));
    }

    private static ArgumentType<PlayerProfilesResolver> profiles(final Predicate<Player> filter) {
        return PlayerProfileArgument.playerProfile(filter, result -> source -> result.getNames(source).stream()
                .map(PlayerProfile::new)
                .toList());
    }

    @Override
    public ArgumentType<GameMode> gameMode() {
        return GameModeArgument.gameMode();
    }

    @Override
    public ArgumentType<UUID> uuid() {
        return UuidArgument.uuid();
    }

    @Override
    public ArgumentType<BlockPositionResolver> blockPosition() {
        return BlockPositionArgument.blockPosition();
    }

    @Override
    public ArgumentType<ColumnPositionResolver> columnPosition() {
        return ColumnPosArgument.columnPos();
    }

    @Override
    public ArgumentType<PositionResolver> position() {
        return Vec3Argument.vec3();
    }

    @Override
    public ArgumentType<AngleResolver> angle() {
        return AngleArgument.angle();
    }

    @Override
    public ArgumentType<World> world() {
        return DimensionArgument.dimension(key -> FidorialServer.getInstance().worlds().manager().world(key));
    }

    @Override
    public ArgumentType<ItemStack> itemStack() {
        return ItemArgument.item(input -> input.createItemStack(1));
    }

    @Override
    public ArgumentType<Predicate<ItemStack>> itemPredicate() {
        return ItemPredicateArgument.itemPredicate();
    }

    @Override
    public ArgumentType<SwingAnimation.SwingAnimationType> swingAnimationType() {
        return SwingAnimationTypeArgument.swingAnimationType();
    }

    @Override
    public ArgumentType<Component> component() {
        return ComponentArgument.textComponent();
    }

    @Override
    public ArgumentType<Style> style() {
        return StyleArgument.style();
    }

    @Override
    public ArgumentType<NamedTextColor> namedColor() {
        return NamedColorArgument.namedColor();
    }

    @Override
    public ArgumentType<TextColor> hexColor() {
        return HexColorArgument.hexColor(TextColor::color);
    }

    @Override
    public ArgumentType<BossBar.Color> bossBarColor() {
        return BossBarColorArgument.bossBarColor();
    }

    @Override
    public ArgumentType<BossBar.Overlay> bossBarOverlay() {
        return BossBarOverlayArgument.bossBarOverlay();
    }

    @Override
    public ArgumentType<BossBar.Flag> bossBarFlag() {
        return BossBarFlagArgument.bossBarFlag();
    }

    @Override
    public ArgumentType<Range<Integer>> integerRange() {
        return RangeArgument.intRange(FidorialArguments::toRange);
    }

    @Override
    public ArgumentType<Range<Double>> doubleRange() {
        return RangeArgument.floatRange(FidorialArguments::toRange);
    }

    private static <N extends Number & Comparable<N>> Range<N> toRange(final RangeBounds<N> bounds) {
        final Optional<N> min = bounds.min();
        final Optional<N> max = bounds.max();

        if (min.isPresent() && max.isPresent()) {
            return Range.closed(min.get(), max.get());
        }
        return min.map(Range::atLeast).orElseGet(() -> max.map(Range::atMost).orElseGet(Range::all));
    }

    @Override
    public ArgumentType<Integer> time(final int minTicks) {
        return TimeArgument.time(minTicks);
    }

    @Override
    public ArgumentType<Duration> duration() {
        return DurationArgument.duration();
    }

    @Override
    public ArgumentType<Key> key() {
        return KeyArgument.key();
    }

    @Override
    public <T> ArgumentType<TypedKey<T>> resourceKey(final RegistryKey<T> registryKey) {
        return ResourceKeyArgument.resourceKey(registryKey);
    }

    @Override
    public <T> ArgumentType<T> resource(final RegistryKey<T> registryKey) {
        return ResourceArgument.resource(registryKey);
    }

    @Override
    public <T> ArgumentType<T> serverResource(final RegistryKey<T> registryKey) {
        final ResourceKeyArgument<T> nativeType = ResourceKeyArgument.resourceKey(registryKey);

        // The registry is resolved on parse, so this can be called while the server is being built.
        return withServerSuggestions(new MappedArgumentType<>(nativeType, (typedKey, reader) ->
                FidorialServer.getInstance().registries().registry(registryKey).find(typedKey)
                        .orElseThrow(() -> ResourceArgument.ERROR_UNKNOWN_RESOURCE
                                .createWithContext(reader, typedKey.key().asString(), registryKey.key().asString()))));
    }

    @Override
    public ArgumentType<Key> postEffect() {
        return withClientSuggestions(KeyArgument.key(), PostEffectArgument.SUGGESTION_SOURCE, PostEffectArgument::suggestBuiltin);
    }

    @Override
    public ArgumentType<NbtPath> nbtPath() {
        return NbtDataArgument.nbtPath();
    }

    @Override
    public <N, T> ArgumentType<T> map(final ArgumentType<N> nativeType, final ArgumentMapper<N, T> mapper) {
        return new MappedArgumentType<>(nativeType, mapper);
    }

    @Override
    public <N, T> ArgumentType<T> map(final ArgumentType<N> nativeType, final ArgumentMapper<N, T> mapper, final SuggestionProvider<CommandSource> suggestions) {
        return new MappedArgumentType<>(nativeType, mapper, suggestions);
    }

    @Override
    public <T> ArgumentType<T> withServerSuggestions(final ArgumentType<T> type) {
        return new ServerSuggestionsArgumentType<>(type);
    }

    @Override
    public <T> ArgumentType<T> withClientSuggestions(final ArgumentType<T> type, final Key suggestionSource, final @Nullable SuggestionProvider<CommandSource> fallbackSuggestions) {
        return new ClientSuggestionsArgumentType<>(type, suggestionSource, fallbackSuggestions);
    }
}
