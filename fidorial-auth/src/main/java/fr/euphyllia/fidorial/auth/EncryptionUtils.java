package fr.euphyllia.fidorial.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

/**
 * The stream cipher of an encrypted connection.
 *
 * <p>Since 26.4-snapshot-9 each direction has its own key and initialization vector, both coming
 * from the {@link SessionKeyExchange}; before, one shared secret served as key and IV of both.</p>
 */
public final class EncryptionUtils {

    private static final String STREAM_ALGORITHM = "AES/CFB8/NoPadding";

    private EncryptionUtils() {
    }

    /**
     * Creates the cipher of one direction of a connection.
     *
     * @param mode {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
     * @param key  the AES key of that direction
     * @param iv   the initialization vector of that direction
     * @return the initialized cipher
     * @throws GeneralSecurityException if AES/CFB8 is not available or the key is invalid
     */
    public static Cipher createStreamCipher(final int mode, final SecretKey key, final IvParameterSpec iv)
            throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(STREAM_ALGORITHM);
        cipher.init(mode, key, iv);
        return cipher;
    }
}
