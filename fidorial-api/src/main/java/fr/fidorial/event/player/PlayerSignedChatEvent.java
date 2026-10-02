package fr.fidorial.event.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;


/**
 * A {@link PlayerChatEvent} for a message that carried a verified client signature.
 *
 * @since 0.1.0
 */
public final class PlayerSignedChatEvent extends PlayerChatEvent {

    private final SignedMessage signedMessage;

    /**
     * Creates an event.
     *
     * @param player        the player who sent the message
     * @param signedMessage the verified signed message
     * @since 0.1.0
     */
    public PlayerSignedChatEvent(final Player player, final SignedMessage signedMessage) {
        super(player, contentOf(signedMessage));
        Preconditions.checkArgument(signedMessage != null, "The signed message of a player signed chat event must not be null");
        this.signedMessage = signedMessage;
    }

    private static Component contentOf(final SignedMessage signedMessage) {
        final Component unsigned = signedMessage.unsignedContent();
        return unsigned != null ? unsigned : Component.text(signedMessage.message());
    }

    /**
     * @return the verified signed message, carrying its signature, timestamp and salt
     * @since 0.1.0
     */
    public SignedMessage signedMessage() {
        return signedMessage;
    }
}
