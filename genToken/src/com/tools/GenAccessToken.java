package com.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;

public class GenAccessToken {
    public static void printUsageAndExit() {
        System.out.println("Usage: getAccessToken <id> <loginName> <password> <salt>");
        System.exit(1);
    }

    /**
     * 对字符串进行散列, 支持md5与sha1算法.
     */
    private static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            if (salt != null) {
                digest.update(salt);
            }

            byte[] result = digest.digest(input);

            for (int i = 1; i < iterations; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对输入字符串进行sha1散列.
     */
    private static final String SHA1 = "SHA-1";

    public static byte[] sha1(byte[] input) {
        return digest(input, SHA1, null, 1);
    }

    public static byte[] sha1(byte[] input, byte[] salt) {
        return digest(input, SHA1, salt, 1);
    }

    public static byte[] sha1(byte[] input, byte[] salt, int iterations) {
        return digest(input, SHA1, salt, iterations);
    }

    /**
     * Hex编码.
     */
    public static String encodeHex(byte[] input) {
        return Hex.encodeHexString(input);
    }

    /**
     * Hex解码.
     */
    public static byte[] decodeHex(String input) {
        try {
            return Hex.decodeHex(input.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(byte[] input) {
        return Base64.encodeBase64String(input);
    }

    public static String getAccessToken(String id, String loginName, String password, String tokenSalt) throws JsonProcessingException {
        final int HASH_INTERATIONS = 1024;
        Map<String, String> data = new HashMap<>();
        data.put("login_name", loginName);
        data.put("id", id);
        byte[] salt = decodeHex(tokenSalt);
        byte[] hashToken = sha1(password.getBytes(), salt, HASH_INTERATIONS);
        data.put("token", encodeHex(hashToken));

        ObjectMapper mapper = new ObjectMapper();

        String dataJson=mapper.writeValueAsString(data);
        return encodeBase64(dataJson.getBytes());
    }

    public static void main(String[] args) {
        if (args == null || args.length < 4) {
            printUsageAndExit();
        }
        String id = args[0];
        String loginName = args[1];
        String password = args[2];
        String salt = args[3];

        try {
            System.out.println(getAccessToken(id, loginName, password, salt));

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
