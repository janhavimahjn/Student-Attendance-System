
package com.attendance.util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String hash(String password)
            throws GeneralSecurityException {

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] result = derive(password, salt, ITERATIONS);

        return ITERATIONS + ":"
            + Base64.getEncoder().encodeToString(salt) + ":"
            + Base64.getEncoder().encodeToString(result);
    }

    public static boolean verify(String password, String stored)
            throws GeneralSecurityException {

        String[] parts = stored.split(":");
        if (parts.length != 3) return false;

        int iterations = Integer.parseInt(parts[0]);
        if (iterations < 100000 || iterations > 1000000) return false;

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expected = Base64.getDecoder().decode(parts[2]);
        byte[] actual = derive(password, salt, iterations);

        if (expected.length != actual.length) return false;

        int difference = 0;
        for (int i = 0; i < expected.length; i++) {
            difference |= expected[i] ^ actual[i];
        }
        return difference == 0;
    }

    private static byte[] derive(String password, byte[] salt, int iterations)
            throws GeneralSecurityException {

        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, iterations, KEY_LENGTH);

        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
