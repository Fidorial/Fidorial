package fr.euphyllia.fidorial.server.chat;

import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public final class SignedMessageHelper {

    private SignedMessageHelper() {
    }

    /**
     * @return a copy of {@code message} whose {@link SignedMessage#unsignedContent()} is
     * {@code unsignedContent}, preserving the signature and, when present, the chain position a
     * vanilla client needs to keep validating this sender's future messages
     */
    public static SignedMessage withUnsignedContent(final SignedMessage message, final @Nullable Component unsignedContent) {
        if (message instanceof final IdentifiedSignedMessage verified) {
            return new IdentifiedSignedMessage(
                    verified.identity(),
                    verified.message(),
                    verified.timestamp(),
                    verified.salt(),
                    verified.signature(),
                    unsignedContent,
                    verified.sessionId(),
                    verified.index(),
                    verified.lastSeen());
        }
        return new UnsignedContentOverride(message, unsignedContent);
    }

    private record UnsignedContentOverride(SignedMessage delegate, @Nullable Component unsignedContent) implements SignedMessage {
        @Override
        public Identity identity() {
            return delegate.identity();
        }

        @Override
        public Instant timestamp() {
            return delegate.timestamp();
        }

        @Override
        public long salt() {
            return delegate.salt();
        }

        @Override
        public @Nullable Signature signature() {
            return delegate.signature();
        }

        @Override
        public String message() {
            return delegate.message();
        }
    }
}
