package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Player;

/**
 * Resolves exactly one player matched by a selector or name.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public non-sealed interface PlayerResolver extends ArgumentResolver<Player> {

    /**
     * {@return the player argument {@code name} matches for the source of {@code context}}
     *
     * @param context the command context
     * @param name    the argument name
     * @throws CommandSyntaxException if it can't be resolved for the source
     * @since 0.1.0
     */
    static Player player(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        return context.getArgument(name, PlayerResolver.class).resolve(context.getSource());
    }
}
