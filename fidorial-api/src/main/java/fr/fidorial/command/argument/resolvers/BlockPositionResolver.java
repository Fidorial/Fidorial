package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;
import fr.fidorial.math.BlockPosition;

/**
 * Resolves a block position, absolute, relative ({@code ~}) or local ({@code ^}).
 *
 * @since 0.1.0
 */
@FunctionalInterface
public non-sealed interface BlockPositionResolver extends ArgumentResolver<BlockPosition> {

    /**
     * {@return the block position argument {@code name}, resolved for the source of {@code context}}
     *
     * @param context the command context
     * @param name    the argument name
     * @throws CommandSyntaxException if it can't be resolved for the source
     * @since 0.1.0
     */
    static BlockPosition blockPosition(final CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
        return context.getArgument(name, BlockPositionResolver.class).resolve(context.getSource());
    }
}
