package fr.fidorial.command;

import fr.fidorial.Server;
import fr.fidorial.permission.PermissionHolder;
import net.kyori.adventure.audience.Audience;

/**
 * Something that can run commands.
 * Receives their feedback and holds the permissions they check.
 *
 * @since 0.1.0
 */
public interface CommandSender extends Audience, PermissionHolder {

    /**
     * {@return the name of this sender}
     *
     * @since 0.1.0
     */
    String name();

    /**
     * {@return the server this sender belongs to}
     *
     * @since 0.1.0
     */
    Server server();

    /**
     * {@return the source a command run by this sender starts from}
     *
     * @since 0.1.0
     */
    CommandSource commandSource();
}
