package org.example.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Verilen düz metni SHA-256 ile hashler ve 64 karakterlik hex string döner.
     * Hem UserService (kayıt/giriş) hem de testler bu metodu kullanır.
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algoritması desteklenmiyor.", e);
        }
    }
}
