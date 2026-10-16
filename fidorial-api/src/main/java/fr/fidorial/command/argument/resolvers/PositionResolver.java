package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;

/**
 * Resolves a position, absolute, relative ({@code ~}) or local ({@code ^}).
 *
 * @since 0.1.0
 */
@FunctionalInterface
public non-sealed interface PositionResolver extends ArgumentResolver<Location> {

    /**
     * {@return the position argument {@code name}, resolved for the source of {@code context}}
     *
     * @param context the command context
     * @param name    the argument name
     * @throws CommandSyntaxException if it can't be resolved for the source
     * @since 0.1.0
     */
    static Location position(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        return context.getArgument(name, PositionResolver.class).resolve(context.getSource());
    }
}
