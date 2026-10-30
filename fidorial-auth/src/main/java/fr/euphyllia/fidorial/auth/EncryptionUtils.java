package fr.euphyllia.fidorial.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public final class EncryptionUtils {

    private EncryptionUtils() {
    }

    public static KeyPair generateServerKeyPair() {
        try {
            final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            return gen.generateKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA indisponible", e);
        }
    }

    public static byte[] generateVerifyToken() {
        final byte[] token = new byte[4];
        new SecureRandom().nextBytes(token);
        return token;
    }

    public static byte[] decryptRsa(final PrivateKey key, final byte[] data) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static SecretKey toAesKey(final byte[] sharedSecret) {
        return new SecretKeySpec(sharedSecret, "AES");
    }

    public static Cipher createStreamCipher(final int mode, final SecretKey key) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(mode, key, new IvParameterSpec(key.getEncoded()));
        return cipher;
    }

    public static String computeServerHash(final String serverId, final byte[] sharedSecret, final PublicKey publicKey) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(serverId.getBytes("ISO_8859_1"));
            digest.update(sharedSecret);
            digest.update(publicKey.getEncoded());
            return new BigInteger(digest.digest()).toString(16);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
