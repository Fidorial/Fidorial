package fr.euphyllia.fidorial.server.chat;

import java.security.PublicKey;
import java.time.Instant;
import java.util.UUID;

/**
 * A player's established signed chat session
 */
public record SignedChatSession(UUID sessionId, PublicKey publicKey, byte[] publicKeyBytes, byte[] signatureBytes, Instant expiresAt) {

    public boolean hasExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
