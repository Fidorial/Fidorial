package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;

/**
 * Resolves an angle in degrees, wrapped to {@code [-180, 180)}, absolute or relative ({@code ~}).
 *
 * @since 0.1.0
 */
@FunctionalInterface
public non-sealed interface AngleResolver extends ArgumentResolver<Float> {

    /**
     * {@return the angle argument {@code name} in degrees, resolved for the source of {@code context}}
     *
     * @param context the command context
     * @param name    the argument name
     * @throws CommandSyntaxException if it can't be resolved for the source
     * @since 0.1.0
     */
    static float angle(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        return context.getArgument(name, AngleResolver.class).resolve(context.getSource());
    }
}
