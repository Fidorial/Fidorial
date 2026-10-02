package fr.fidorial.command.argument.resolvers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.fidorial.command.CommandSource;
import org.jetbrains.annotations.ApiStatus;

/**
 * An {@link ArgumentResolver} is capable of resolving
 * an argument value using a {@link CommandSource}.
 *
 * @param <T> resolved type
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface ArgumentResolver<T> {

    /**
     * Resolves the argument with the given
     * command source.
     *
     * @param source source
     * @return resolved
     * @throws CommandSyntaxException if an error occurs while parsing
     */
    T resolve(CommandSource source) throws CommandSyntaxException;
}
