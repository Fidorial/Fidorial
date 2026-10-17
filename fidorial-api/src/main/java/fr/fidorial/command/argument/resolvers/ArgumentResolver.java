package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;

/**
 * An argument resolved against the source running the command, not the one it was parsed with.
 *
 * <p>{@literal /execute} parses once, then changes the executor and location. Anything depending on
 * them (selectors, relative or local coordinates) is resolved here.</p>
 *
 * <p>Each subtype reads its argument with a static method named after its factory in
 * {@link fr.fidorial.command.argument.Arguments Arguments}, like {@link PlayersResolver#players}.</p>
 *
 * @param <T> the resolved type
 * @since 0.1.0
 */
public sealed interface ArgumentResolver<T> permits
        EntityResolver, EntitiesResolver, PlayerResolver, PlayersResolver, PlayerProfilesResolver,
        BlockPositionResolver, ColumnPositionResolver, PositionResolver, AngleResolver {

    /**
     * {@return this argument resolved for {@code source}}
     *
     * @param source the source running the command
     * @throws CommandSyntaxException if it can't be resolved for {@code source}
     * @since 0.1.0
     */
    T resolve(CommandSource source) throws CommandSyntaxException;
}
