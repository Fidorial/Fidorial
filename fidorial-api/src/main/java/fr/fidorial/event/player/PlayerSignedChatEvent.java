package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;

import java.util.Objects;

/**
 * A {@link PlayerChatEvent} for a message that carried a verified client signature.
 *
 * @since 0.1.0
 */
public final class PlayerSignedChatEvent extends PlayerChatEvent {

    private final SignedMessage signedMessage;

    public PlayerSignedChatEvent(final Player player, final SignedMessage signedMessage) {
        super(player, contentOf(signedMessage));
        this.signedMessage = Objects.requireNonNull(signedMessage, "signedMessage");
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
