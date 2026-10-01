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

    public PlayerSignedChatEvent(final Player player, final SignedMessage signedMessage) {
        super(player, contentOf(signedMessage));
        this.signedMessage = Preconditions.checkNotNull(signedMessage, "The signed message of a player signed chat event must not be null");
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
