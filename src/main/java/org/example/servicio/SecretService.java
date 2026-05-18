package org.example.servicio;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class SecretService {

    private static final String PREFIX = "enc:v1:";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecretService() {
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return "";
        if (plainText.startsWith(PREFIX)) return plainText;
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar el secreto.", e);
        }
    }

    public static String decrypt(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) return "";
        if (!storedValue.startsWith(PREFIX)) return storedValue;
        try {
            String payload = storedValue.substring(PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) return "";
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static SecretKeySpec key() throws Exception {
        String configured = System.getProperty("pos.secret.key");
        if (configured == null || configured.isBlank()) configured = System.getenv("POS_SECRET_KEY");
        if (configured == null || configured.isBlank()) {
            configured = System.getProperty("user.name", "pos") + "|"
                    + System.getProperty("user.home", "") + "|volovan-volo-pos-local-key";
        }
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(configured.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(digest, "AES");
    }
}
