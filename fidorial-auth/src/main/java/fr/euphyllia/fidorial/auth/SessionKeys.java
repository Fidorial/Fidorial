package fr.euphyllia.fidorial.auth;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

/**
 * The keys agreed on by a {@link SessionKeyExchange}, seen from the server ({@code A}) towards a
 * client ({@code B}).
 *
 * @param protocolVersion         the protocol version of the connection
 * @param salt                    the salt announced to the client
 * @param authenticationRequested whether the client was asked to authenticate with Mojang
 * @param server                  the server's public key and name
 * @param client                  the client's public key and name
 * @param streamAToB              the AES key of what the server sends
 * @param streamBToA              the AES key of what the client sends
 * @param authSignature           the HMAC key of the digest sent to Mojang's session server
 * @param integritySignature      the HMAC key of the session's integrity digest
 */
public record SessionKeys(
        int protocolVersion,
        long salt,
        boolean authenticationRequested,
        Peer server,
        Peer client,
        SecretKey streamAToB,
        SecretKey streamBToA,
        SecretKey authSignature,
        SecretKey integritySignature
) {

    /**
     * The MAC of both digests.
     */
    static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    /**
     * The length of the integrity digest the client sends in the key packet.
     */
    public static final int INTEGRITY_DIGEST_LENGTH = 32;

    private static final byte DIGEST_SEPARATOR = '+';
    private static final int AUTHENTICATION_DIGEST_BYTES = 25;
    private static final int IV_BYTES = 16;
    private static final char[] BASE32_HEX = "0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray();

    /**
     * One end of the exchange.
     *
     * @param publicKey its X25519 public key
     * @param name      the name it uses in the derivation: {@link SessionKeyExchange#SERVER_NAME} for
     *                  the server, the player name for the client
     */
    public record Peer(PublicKey publicKey, String name) {

        private void addToDigest(final Mac mac) {
            mac.update(name.getBytes(StandardCharsets.UTF_8));
            mac.update(DIGEST_SEPARATOR);
            mac.update(publicKey.getEncoded());
        }
    }

    /**
     * The initialization vectors of both streams, drawn from the integrity digest.
     *
     * @param serverToClient the IV of what the server sends
     * @param clientToServer the IV of what the client sends
     */
    public record InitializationVectors(IvParameterSpec serverToClient, IvParameterSpec clientToServer) {
    }

    /**
     * The digest the server gives Mojang's session server in place of the old SHA-1
     * {@code serverId}: the first 25 bytes of an HMAC over both public keys, in upper case base32hex.
     *
     * @return the 40 characters of the digest
     */
    public String computeKeysAuthenticationDigest() {
        try {
            final Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(authSignature);
            mac.update(server.publicKey().getEncoded());
            mac.update(DIGEST_SEPARATOR);
            mac.update(client.publicKey().getEncoded());
            return base32Hex(Arrays.copyOf(mac.doFinal(), AUTHENTICATION_DIGEST_BYTES));
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Cannot compute the authentication digest", e);
        }
    }

    /**
     * The digest of the session parameters both ends computed. The client sends its own in the key
     * packet; a mismatch means the exchange was tampered with.
     *
     * @return the {@value #INTEGRITY_DIGEST_LENGTH} bytes of the digest
     */
    public byte[] computeIntegrityDigest() {
        try {
            final Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            mac.init(integritySignature);
            mac.update(ByteBuffer.allocate(Integer.BYTES).putInt(protocolVersion).array());
            mac.update(DIGEST_SEPARATOR);
            mac.update(ByteBuffer.allocate(Long.BYTES).putLong(salt).array());
            mac.update(DIGEST_SEPARATOR);
            mac.update((byte) (authenticationRequested ? 1 : 2));
            mac.update(DIGEST_SEPARATOR);
            server.addToDigest(mac);
            mac.update(DIGEST_SEPARATOR);
            client.addToDigest(mac);
            return mac.doFinal();
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Cannot compute the integrity digest", e);
        }
    }

    /**
     * Checks the integrity digest a client sent, in constant time.
     *
     * @param signature the digest from the key packet
     * @return whether it matches the one computed by the server
     */
    public boolean verifyIntegrity(final byte[] signature) {
        return MessageDigest.isEqual(computeIntegrityDigest(), signature);
    }

    /**
     * Draws the initialization vectors of both streams. They come from a {@link Random} seeded
     * with the hash of the integrity digest, as the client draws them: first the server's, then the
     * client's.
     *
     * @param integrityDigest the integrity digest of the session
     * @return the initialization vectors
     */
    public static InitializationVectors initializationVectors(final byte[] integrityDigest) {
        final Random random = new Random(Arrays.hashCode(integrityDigest));
        final byte[] serverToClient = new byte[IV_BYTES];
        random.nextBytes(serverToClient);
        final byte[] clientToServer = new byte[IV_BYTES];
        random.nextBytes(clientToServer);
        return new InitializationVectors(new IvParameterSpec(serverToClient), new IvParameterSpec(clientToServer));
    }

    /**
     * Encodes in base32hex (RFC 4648, upper case), padded with {@code =}.
     */
    static String base32Hex(final byte[] data) {
        final StringBuilder out = new StringBuilder((data.length + 4) / 5 * 8);
        int buffer = 0;
        int bits = 0;
        for (final byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                out.append(BASE32_HEX[(buffer >> bits) & 0x1F]);
            }
        }
        if (bits > 0) {
            out.append(BASE32_HEX[(buffer << (5 - bits)) & 0x1F]);
        }
        while (out.length() % 8 != 0) {
            out.append('=');
        }
        return out.toString();
    }
}
