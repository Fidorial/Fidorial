package fr.euphyllia.fidorial.server.network.proxy;

import fr.euphyllia.fidorial.auth.GameProfile;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VelocityForwarding {

    public static final Key CHANNEL = Key.key("velocity", "player_info");

    public static final int MODERN_DEFAULT = 1;
    public static final int MODERN_WITH_KEY = 2;
    public static final int MODERN_WITH_KEY_V2 = 3;
    public static final int MODERN_LAZY_SESSION = 4;

    public static final int MAX_SUPPORTED_VERSION = MODERN_LAZY_SESSION;

    private static final String ALGORITHM = "HmacSHA256";
    private static final int SIGNATURE_LENGTH = 32;

    private VelocityForwarding() {
    }

    public record ForwardedData(String remoteAddress, GameProfile profile) {
    }

    public static ForwardedData decode(final byte[] payload, final String secret) throws ForwardingException {
        if (payload.length <= SIGNATURE_LENGTH) {
            throw new ForwardingException("Forwarding payload too short");
        }

        final byte[] signature = new byte[SIGNATURE_LENGTH];
        System.arraycopy(payload, 0, signature, 0, SIGNATURE_LENGTH);

        final byte[] data = new byte[payload.length - SIGNATURE_LENGTH];
        System.arraycopy(payload, SIGNATURE_LENGTH, data, 0, data.length);

        byte[] expected;
        try {
            final Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            expected = mac.doFinal(data);
        } catch (final GeneralSecurityException e) {
            throw new ForwardingException("Unable to calculate the HMAC", e);
        }
        if (!MessageDigest.isEqual(expected, signature)) {
            throw new ForwardingException(
                    "Invalid signature: the Velocity secret does not match.");
        }

        final PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(data));
        try {
            final int version = buf.readVarInt();
            if (version == MODERN_WITH_KEY || version == MODERN_WITH_KEY_V2) {
                throw new ForwardingException("Forwarding " + version + "(with player key) not supported; the client is too old.");
            }
            if (version != MODERN_DEFAULT && version != MODERN_LAZY_SESSION) {
                throw new ForwardingException("Unknown forwarding version:" + version);
            }

            final String address = buf.readString(Short.MAX_VALUE);
            final UUID uuid = buf.readUuid();
            final String username = buf.readString(16);

            final int propertyCount = buf.readVarInt();
            final List<GameProfile.Property> properties = new ArrayList<>(propertyCount);
            for (int i = 0; i < propertyCount; i++) {
                final String name = buf.readString(Short.MAX_VALUE);
                final String value = buf.readString(Short.MAX_VALUE);
                final String sig = buf.readBoolean() ? buf.readString(Short.MAX_VALUE) : null;
                properties.add(new GameProfile.Property(name, value, sig));
            }

            return new ForwardedData(address, new GameProfile(uuid, username, uuid, properties));
        } catch (final ForwardingException e) {
            throw e;
        } catch (final Exception e) {
            throw new ForwardingException("Malformed forwarding payload", e);
        } finally {
            buf.nettyBuf().release();
        }
    }

    public static final class ForwardingException extends Exception {
        public ForwardingException(final String message) {
            super(message);
        }

        public ForwardingException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
