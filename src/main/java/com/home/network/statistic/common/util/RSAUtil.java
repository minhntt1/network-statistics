package com.home.network.statistic.common.util;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class RSAUtil {
    @SneakyThrows
    public static String encrypt(String data, String modulusHex, String exponentHex)  {
        BigInteger modulus = new BigInteger(modulusHex, 16);
        BigInteger exponent = new BigInteger(exponentHex, 16);

        java.security.spec.RSAPublicKeySpec spec =
                new java.security.spec.RSAPublicKeySpec(modulus, exponent);

        java.security.KeyFactory factory = java.security.KeyFactory.getInstance("RSA");
        java.security.PublicKey publicKey = factory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encrypted);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
