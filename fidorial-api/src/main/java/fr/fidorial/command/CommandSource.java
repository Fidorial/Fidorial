package fr.fidorial.command;

import com.google.common.base.Preconditions;
import fr.fidorial.Server;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jspecify.annotations.Nullable;

/**
 * The context a command runs in. {@literal /execute} can change the executor and location,
 * but never the sender. Messages sent to a source go to its sender.
 *
 * @since 0.1.0
 */
public sealed interface CommandSource extends ForwardingAudience.Single permits CommandSourceImpl {

    /**
     * {@return a source for {@code sender}, with no executor or location}
     *
     * @param sender who runs the command
     * @since 0.1.0
     */
    static CommandSource of(final CommandSender sender) {
        Preconditions.checkArgument(sender != null, "sender cannot be null");
        return new CommandSourceImpl(sender, null, null);
    }

    /**
     * {@return who ran the command}
     *
     * @since 0.1.0
     */
    CommandSender sender();

    /**
     * {@return the entity the command acts as, or {@code null} if none}
     *
     * @since 0.1.0
     */
    @Nullable Entity executor();

    /**
     * {@return where the command runs, or {@code null} if no world is loaded}
     *
     * @since 0.1.0
     */
    @Nullable Location location();

    /**
     * {@return a copy of this source acting as {@code executor}, like {@literal /execute as}}
     *
     * @since 0.1.0
     */
    CommandSource as(Entity executor);

    /**
     * {@return a copy of this source running at {@code location}, like {@literal /execute at}}
     *
     * @since 0.1.0
     */
    CommandSource at(Location location);

    /**
     * {@return the server the command runs on}
     *
     * @since 0.1.0
     */
    default Server server() {
        return sender().server();
    }

    @Override
    default Audience audience() {
        return sender();
    }
}
