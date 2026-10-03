package fr.fidorial.command;

import fr.fidorial.Server;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

/**
 * @since 0.1.0
 */
public interface CommandSource {
    /**
     * Gets the location that this command is being executed at.
     *
     * @return a cloned location instance.
     * @since 0.1.0
     */
    Location location();

    /**
     * Gets the {@link CommandSender} that executed this command.
     * The sender of a command is the one that triggered the execution of a command.
     * It differs to {@link #executor()} as the executor can be changed by a command, e.g. {@literal /execute}.
     *
     * @return the command sender instance
     * @since 0.1.0
     */
    CommandSender sender();

    /**
     * Gets the entity that executes this command.
     * May not always be {@link #sender()} as the executor of a command can be changed to a different entity
     * than the one that triggered the command.
     *
     * @return entity that executes this command
     * @since 0.1.0
     */
    @Nullable Entity executor();

    /**
     * Gets the server associated with this source.
     *
     * @return the server
     * @since 0.1.0
     */
    Server server();
}
