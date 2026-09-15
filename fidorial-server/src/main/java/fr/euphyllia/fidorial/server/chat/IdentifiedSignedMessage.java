package fr.euphyllia.fidorial.server.chat;

import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IdentifiedSignedMessage(
        Identity identity,
        String message,
        Instant timestamp,
        long salt,
        SignedMessage.Signature signature,
        @Nullable Component unsignedContent,
        UUID sessionId,
        int index,
        List<SignedMessage.Signature> lastSeen
) implements SignedMessage {

    @Override
    public Signature signature() {
        return signature;
    }
}
