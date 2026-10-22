package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

/**
 * Flags carried by {@code apply_options} and {@code login_finished} (vanilla {@code ConnectionOptionsPacket}).
 */
public final class ConnectionOptions {

    /**
     * Switch on encryption with the shared secret from the key exchange.
     */
    public static final byte ENCRYPTION = 1;

    /**
     * Switch on compression with the threshold sent in {@code setup_compression}.
     */
    public static final byte COMPRESSION = 2;

    private ConnectionOptions() {
    }

    public static boolean isEncryption(final byte options) {
        return (options & ENCRYPTION) != 0;
    }

    public static boolean isCompression(final byte options) {
        return (options & COMPRESSION) != 0;
    }
}
