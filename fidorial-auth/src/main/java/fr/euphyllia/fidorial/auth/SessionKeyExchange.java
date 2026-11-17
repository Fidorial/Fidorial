package fr.euphyllia.fidorial.auth;

import javax.crypto.KDF;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;

/**
 * The server side of the login key exchange used since 26.4-snapshot-9, replacing the RSA
 * encrypted shared secret.
 *
 * <p>Both ends hold an X25519 key pair. The server announces its public key and a per-connection
 * salt in the {@code hello} (key) packet, the client answers with its own public key and the
 * integrity digest of the session. The shared secret of the X25519 agreement goes through HKDF
 * (SHA-256) to derive one AES key per direction and two HMAC keys: one whose digest replaces the
 * old SHA-1 {@code serverId} sent to Mojang's session server, one that signs the session
 * parameters so that the server can detect tampering.</p>
 *
 * <p>The server is side {@code A} of the exchange, the client side {@code B}.</p>
 *
 * @see SessionKeys
 */
public final class SessionKeyExchange {

    /**
     * The name the server gives itself in the exchange, sent as the {@code serverId} of the key
     * packet.
     */
    public static final String SERVER_NAME = "server";

    /**
     * The longest {@code serverId} the client accepts.
     */
    public static final int MAX_SERVER_NAME_LENGTH = 20;

    /**
     * The longest encoded public key read from the client.
     */
    public static final int MAX_ENCODED_KEY_LENGTH = 256 + 128;

    private static final String KEY_ALGORITHM = "XDH";
    private static final NamedParameterSpec CURVE = NamedParameterSpec.X25519;
    private static final String KDF_ALGORITHM = "HKDF-SHA256";
    private static final String STREAM_KEY_ALGORITHM = "AES";
    private static final int STREAM_KEY_BYTES = 16;
    private static final int SIGNATURE_KEY_BYTES = 32;

    /**
     * The fixed part of the HKDF salt, after the per-connection salt.
     */
    private static final byte[] SALT = ByteBuffer.allocate(Long.BYTES * 3)
            .putLong(3195254534265565831L)
            .putLong(8302438179253405948L)
            .putLong(6794457759077931664L)
            .array();

    private final int protocolVersion;
    private final long salt;
    private final boolean authenticationRequested;
    private final String serverName;
    private final KeyPair keyPair;

    private SessionKeyExchange(final int protocolVersion, final long salt, final boolean authenticationRequested,
                               final String serverName, final KeyPair keyPair) {
        this.protocolVersion = protocolVersion;
        this.salt = salt;
        this.authenticationRequested = authenticationRequested;
        this.serverName = serverName;
        this.keyPair = keyPair;
    }

    /**
     * Generates an X25519 key pair.
     *
     * @param random the source of randomness
     * @return the new key pair
     */
    public static KeyPair createKeyPair(final SecureRandom random) {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            generator.initialize(CURVE, random);
            return generator.generateKeyPair();
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("X25519 is not available", e);
        }
    }

    /**
     * Reads a public key sent by a client.
     *
     * @param encoded the X.509 encoding of the key
     * @return the key
     * @throws GeneralSecurityException if the bytes are not an X.509 encoded X25519 public key
     */
    public static PublicKey decodePublicKey(final byte[] encoded) throws GeneralSecurityException {
        final PublicKey key = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(encoded));
        if (!(key instanceof final XECPublicKey xec)
                || !(xec.getParams() instanceof final NamedParameterSpec named)
                || !named.getName().equals(CURVE.getName())) {
            throw new GeneralSecurityException("Not an X25519 public key");
        }
        return key;
    }

    /**
     * @return the salt announced to the client, mixed into the derivation of the session keys
     */
    public long salt() {
        return salt;
    }

    /**
     * @return whether the client is asked to authenticate with Mojang's session server
     */
    public boolean authenticationRequested() {
        return authenticationRequested;
    }

    /**
     * @return the name the server gives itself, sent as the {@code serverId} of the key packet
     */
    public String serverName() {
        return serverName;
    }

    /**
     * @return the X.509 encoding of the server's public key, as sent in the key packet
     */
    public byte[] encodedPublicKey() {
        final byte[] encoded = keyPair.getPublic().getEncoded();
        if (encoded == null) {
            throw new IllegalStateException("The server public key has no encoded form");
        }
        return encoded;
    }

    /**
     * Agrees on the keys of the session with a client.
     *
     * @param clientKey  the client's X25519 public key
     * @param clientName the name the client logged in with
     * @return the keys of the session
     * @throws GeneralSecurityException if the agreement or the derivation fails
     */
    public SessionKeys createSessionKeys(final PublicKey clientKey, final String clientName)
            throws GeneralSecurityException {
        final SessionKeys.Peer server = new SessionKeys.Peer(keyPair.getPublic(), serverName);
        final SessionKeys.Peer client = new SessionKeys.Peer(clientKey, clientName);

        final KeyAgreement agreement = KeyAgreement.getInstance(KEY_ALGORITHM);
        agreement.init(keyPair.getPrivate(), CURVE);
        agreement.doPhase(clientKey, true);
        final byte[] sharedSecret = agreement.generateSecret();

        final String connectionParams = "a=" + server.name() + ";b=" + client.name() + ";protocol=" + protocolVersion;
        final String authParams = "a=" + server.name() + ";b=" + client.name();

        final KDF kdf = KDF.getInstance(KDF_ALGORITHM);
        final HKDFParameterSpec.Builder extract = HKDFParameterSpec.ofExtract()
                .addIKM(sharedSecret)
                .addSalt(ByteBuffer.allocate(Long.BYTES).putLong(salt).array())
                .addSalt(SALT);

        final SecretKey aToB = kdf.deriveKey(STREAM_KEY_ALGORITHM,
                extract.thenExpand(info("a_to_b;" + connectionParams), STREAM_KEY_BYTES));
        final SecretKey bToA = kdf.deriveKey(STREAM_KEY_ALGORITHM,
                extract.thenExpand(info("b_to_a;" + connectionParams), STREAM_KEY_BYTES));
        final SecretKey authSignature = kdf.deriveKey(SessionKeys.SIGNATURE_ALGORITHM,
                extract.thenExpand(info("authenticate;" + authParams), SIGNATURE_KEY_BYTES));
        final SecretKey integritySignature = kdf.deriveKey(SessionKeys.SIGNATURE_ALGORITHM,
                extract.thenExpand(info("verify;" + authParams), SIGNATURE_KEY_BYTES));

        return new SessionKeys(protocolVersion, salt, authenticationRequested, server, client,
                aToB, bToA, authSignature, integritySignature);
    }

    private static byte[] info(final String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Hands out the server side of each exchange. The key pair is shared between connections and
     * renewed once it is older than the rotation period, the salt is new for every connection.
     */
    public static final class ServerSideFactory {

        private final SecureRandom random = new SecureRandom();
        private final long rotationPeriodMillis;
        private KeyPair keyPair;
        private long keyPairCreatedAt;

        /**
         * @param rotationPeriod the minimum age of a key pair before it is replaced;
         *                       {@link Duration#ZERO} for a new key pair on every connection
         */
        public ServerSideFactory(final Duration rotationPeriod) {
            if (rotationPeriod.isNegative()) {
                throw new IllegalArgumentException("The key rotation period cannot be negative: " + rotationPeriod);
            }
            this.rotationPeriodMillis = rotationPeriod.toMillis();
            rotate();
        }

        private void rotate() {
            this.keyPair = createKeyPair(random);
            this.keyPairCreatedAt = System.currentTimeMillis();
        }

        /**
         * Starts the exchange of one connection.
         *
         * @param serverName              the name the server gives itself, at most
         *                                {@value #MAX_SERVER_NAME_LENGTH} characters
         * @param protocolVersion         the protocol version of the connection
         * @param authenticationRequested whether the client must authenticate with Mojang
         * @return the server side of the exchange
         */
        public synchronized SessionKeyExchange create(final String serverName, final int protocolVersion,
                                                      final boolean authenticationRequested) {
            if (serverName.length() > MAX_SERVER_NAME_LENGTH) {
                throw new IllegalArgumentException("Server name longer than " + MAX_SERVER_NAME_LENGTH + " characters: " + serverName);
            }
            if (rotationPeriodMillis == 0 || System.currentTimeMillis() - keyPairCreatedAt > rotationPeriodMillis) {
                rotate();
            }
            return new SessionKeyExchange(protocolVersion, random.nextLong(), authenticationRequested, serverName, keyPair);
        }
    }
}
