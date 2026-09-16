package fr.euphyllia.fidorial.server.chat;

import net.kyori.adventure.chat.SignedMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ChatSigning {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SESSION_SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final String MESSAGE_SIGNATURE_ALGORITHM = "SHA256withRSA";

    private ChatSigning() {
    }

    public static PublicKey decodePublicKey(final byte[] x509) throws GeneralSecurityException {
        return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(x509));
    }

    public static boolean verifySessionSignature(
            final PublicKey mojangKey,
            final UUID playerUuid,
            final long expiresAtEpochMillis,
            final byte[] publicKeyBytes,
            final byte[] signature
    ) throws GeneralSecurityException {
        final Signature verifier = Signature.getInstance(SESSION_SIGNATURE_ALGORITHM);
        verifier.initVerify(mojangKey);
        verifier.update(uuidBytes(playerUuid));
        verifier.update(longBytes(expiresAtEpochMillis));
        verifier.update(publicKeyBytes);
        return verifier.verify(signature);
    }

    /**
     * Verifies a signed chat message against the sender's session public key.
     *
     * @param senderKey the sender's session public key
     * @param sender    the sender's identity
     * @param sessionId the sender's chat session id
     * @param index     the message's position in the sender's signature chain
     * @param salt      the message's salt
     * @param timestamp the message's timestamp
     * @param content   the raw, unsigned message content
     * @param lastSeen  the signatures the sender claims to have last seen, oldest first
     * @param signature the signature to verify
     */
    public static boolean verifyMessageSignature(
            final PublicKey senderKey,
            final UUID sender,
            final UUID sessionId,
            final int index,
            final long salt,
            final Instant timestamp,
            final String content,
            final List<SignedMessage.Signature> lastSeen,
            final byte[] signature
    ) throws GeneralSecurityException {
        final Signature verifier = Signature.getInstance(MESSAGE_SIGNATURE_ALGORITHM);
        verifier.initVerify(senderKey);
        updateMessageSignature(verifier::update, sender, sessionId, index, salt, timestamp, content, lastSeen);
        return verifier.verify(signature);
    }

    public static void updateMessageSignature(
            final ByteUpdater updater,
            final UUID sender,
            final UUID sessionId,
            final int index,
            final long salt,
            final Instant timestamp,
            final String content,
            final List<SignedMessage.Signature> lastSeen
    ) throws SignatureException {
        updater.update(intBytes(1)); // format version
        updater.update(uuidBytes(sender));
        updater.update(uuidBytes(sessionId));
        updater.update(intBytes(index));
        updater.update(longBytes(salt));
        updater.update(longBytes(timestamp.getEpochSecond()));

        final byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        updater.update(intBytes(contentBytes.length));
        updater.update(contentBytes);

        updater.update(intBytes(lastSeen.size()));
        for (final SignedMessage.Signature seen : lastSeen) {
            updater.update(seen.bytes());
        }
    }

    private static byte[] uuidBytes(final UUID uuid) {
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    private static byte[] intBytes(final int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    private static byte[] longBytes(final long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    @FunctionalInterface
    public interface ByteUpdater {
        void update(byte[] bytes) throws SignatureException;
    }
}
