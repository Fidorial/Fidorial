package fr.euphyllia.fidorial.server.chat;

import net.kyori.adventure.chat.SignedMessage;
import org.jspecify.annotations.Nullable;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SignedMessageChain {

    private final UUID sender;

    private int nextIndex;
    private Instant lastTimestamp = Instant.EPOCH;
    private boolean broken;

    public SignedMessageChain(final UUID sender) {
        this.sender = sender;
    }

    public boolean isBroken() {
        return broken;
    }

    public void markBroken() {
        this.broken = true;
    }

    /**
     * Verifies and, on success, advances the chain for one incoming message.
     *
     * @return the verified index and signature, or a failure reason
     */
    public Outcome verify(
            final SignedChatSession session,
            final long salt,
            final Instant timestamp,
            final String content,
            final List<SignedMessage.Signature> lastSeen,
            final byte @Nullable [] signatureBytes
    ) {
        if (broken) {
            return Outcome.failure(Reason.CHAIN_BROKEN);
        }
        if (signatureBytes == null) {
            broken = true;
            return Outcome.failure(Reason.MISSING_SIGNATURE);
        }
        if (session.hasExpired()) {
            broken = true;
            return Outcome.failure(Reason.EXPIRED_KEY);
        }
        if (timestamp.isBefore(lastTimestamp)) {
            broken = true;
            return Outcome.failure(Reason.OUT_OF_ORDER);
        }

        final int index = nextIndex;
        final boolean verified;
        try {
            verified = ChatSigning.verifyMessageSignature(
                    session.publicKey(), sender, session.sessionId(), index, salt, timestamp, content, lastSeen, signatureBytes);
        } catch (final GeneralSecurityException e) {
            broken = true;
            return Outcome.failure(Reason.INVALID_SIGNATURE);
        }

        if (!verified) {
            broken = true;
            return Outcome.failure(Reason.INVALID_SIGNATURE);
        }

        this.lastTimestamp = timestamp;
        this.nextIndex = index + 1;
        return Outcome.success(index, SignedMessage.signature(signatureBytes));
    }

    public enum Reason {
        MISSING_SIGNATURE,
        EXPIRED_KEY,
        INVALID_SIGNATURE,
        OUT_OF_ORDER,
        CHAIN_BROKEN
    }

    public record Outcome(boolean verified, int index, SignedMessage.@Nullable Signature signature, @Nullable Reason reason) {

        static Outcome success(final int index, final SignedMessage.Signature signature) {
            return new Outcome(true, index, signature, null);
        }

        static Outcome failure(final Reason reason) {
            return new Outcome(false, -1, null, reason);
        }
    }
}
