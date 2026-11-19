package fr.euphyllia.fidorial.testplugin.transfer;

import net.kyori.adventure.key.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Signed tickets carried in a cookie from one server to the next, so that the server a player is
 * transferred to can check that one of its own sent him.
 *
 * <p>The client sends back whatever it likes, so a ticket is signed: it holds the uuid of the player and
 * an expiry, followed by their HMAC-SHA256. Every server must share the same secret, given with
 * {@code -Dfidorial.test.transfer-secret=...}. A ticket can be used again until it expires: a real plugin
 * would also remember the tickets already used.</p>
 */
public final class TransferTickets {

    /**
     * The cookie the ticket is stored in.
     */
    public static final Key COOKIE = Key.key("fidorial-testplugin", "transfer_ticket");

    private static final String SECRET_PROPERTY = "fidorial.test.transfer-secret";
    private static final String DEFAULT_SECRET = "fidorial-test-secret";
    private static final String ALGORITHM = "HmacSHA256";
    private static final Duration LIFETIME = Duration.ofSeconds(30);

    // The uuid (two longs) and the expiry, then the signature.
    private static final int BODY_SIZE = Long.BYTES * 3;
    private static final int SIGNATURE_SIZE = 32;

    private final SecretKeySpec secret;
    private final boolean defaultSecret;

    private TransferTickets(final String secret, final boolean defaultSecret) {
        this.secret = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.defaultSecret = defaultSecret;
    }

    /**
     * Creates the tickets signed with the secret of {@code -Dfidorial.test.transfer-secret}, or with a
     * secret everybody knows when it is not set.
     *
     * @return the tickets
     */
    public static TransferTickets fromSystemProperty() {
        final String secret = System.getProperty(SECRET_PROPERTY);
        return secret == null || secret.isBlank()
                ? new TransferTickets(DEFAULT_SECRET, true)
                : new TransferTickets(secret, false);
    }

    /**
     * {@return whether the tickets are signed with the default secret, which proves nothing}
     */
    public boolean usesDefaultSecret() {
        return defaultSecret;
    }

    /**
     * Issues a ticket for a player about to be transferred.
     *
     * @param player the uuid of the player
     * @return the content of the cookie
     */
    public byte[] issue(final UUID player) {
        final ByteBuffer ticket = ByteBuffer.allocate(BODY_SIZE + SIGNATURE_SIZE)
                .putLong(player.getMostSignificantBits())
                .putLong(player.getLeastSignificantBits())
                .putLong(System.currentTimeMillis() + LIFETIME.toMillis());
        ticket.put(sign(ticket.array()));
        return ticket.array();
    }

    /**
     * Checks the ticket a client sent back.
     *
     * @param cookie the content of the cookie, empty if the client has none
     * @param player the uuid of the player logging in
     * @return what is wrong with the ticket, or {@link Verdict#VALID}
     */
    public Verdict verify(final Optional<byte[]> cookie, final UUID player) {
        if (cookie.isEmpty()) {
            return Verdict.MISSING;
        }
        final byte[] ticket = cookie.get();
        if (ticket.length != BODY_SIZE + SIGNATURE_SIZE) {
            return Verdict.MALFORMED;
        }
        final byte[] signature = Arrays.copyOfRange(ticket, BODY_SIZE, ticket.length);
        if (!MessageDigest.isEqual(sign(ticket), signature)) {
            return Verdict.FORGED;
        }
        final ByteBuffer body = ByteBuffer.wrap(ticket);
        if (!new UUID(body.getLong(), body.getLong()).equals(player)) {
            return Verdict.WRONG_PLAYER;
        }
        if (body.getLong() < System.currentTimeMillis()) {
            return Verdict.EXPIRED;
        }
        return Verdict.VALID;
    }

    private byte[] sign(final byte[] ticket) {
        try {
            final Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secret);
            mac.update(ticket, 0, BODY_SIZE);
            return mac.doFinal();
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Every Java runtime provides " + ALGORITHM, e);
        }
    }

    /**
     * What a ticket check found.
     */
    public enum Verdict {
        VALID,
        MISSING,
        MALFORMED,
        FORGED,
        WRONG_PLAYER,
        EXPIRED
    }
}
