package fr.fidorial.command.argument.resolvers;

import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Position;
import org.jetbrains.annotations.ApiStatus;

/**
 * An {@link ArgumentResolver} that's capable of resolving
 * argument value using a {@link CommandSource}.
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface PositionResolver extends ArgumentResolver<Position> {

    Position resolve(CommandSource source);
}
