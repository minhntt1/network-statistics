package com.home.network.statistic.common.util;

import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class EncryptionUtil {
    private static final SecureRandom random = new SecureRandom();

    public static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @SneakyThrows
    public static String md5(String input) {
        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] messageDigest = md.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
