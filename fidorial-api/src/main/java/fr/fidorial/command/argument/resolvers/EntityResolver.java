package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;

/**
 * Resolves exactly one entity matched by a selector, name or UUID.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public non-sealed interface EntityResolver extends ArgumentResolver<Entity> {

    /**
     * {@return the entity argument {@code name} matches for the source of {@code context}}
     *
     * @param context the command context
     * @param name    the argument name
     * @throws CommandSyntaxException if it can't be resolved for the source
     * @since 0.1.0
     */
    static Entity entity(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        return context.getArgument(name, EntityResolver.class).resolve(context.getSource());
    }
}
