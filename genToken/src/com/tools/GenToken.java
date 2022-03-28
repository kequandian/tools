package com.tools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.sql.Date;
import java.util.Base64;
import java.util.IllegalFormatException;
import java.util.Random;

public class GenToken {
    private static final String encodedKey = "L7A/6zARSkK1j7Vd5SDD9pSSqZlqF7mAhiOgRbgv9Smce6tf4cJnvKOjtKPxNNnWQj+2lQEScm3XIUjhW+YVZg==";
    private static final String accessEncodedKey = "bm9ybWFsLWVuY29kZWQta2V5";

    // private long ttlMillis = 2592000000L;  //30 days
    // private String tokenType = "Bearer";
    // private Boolean enableAttemptLogin = true;
    // private Boolean nonShiroPermissionCheck = false;
   
    public static void printUsage() {
        System.out.println("Usage: genToken <orgId> <userId> <account> <ttl>");
        System.out.println("       genToken access <orgId> <userId> <account> <domainUserId> <ttl>");
        System.out.println("       genToken gen password <password> [salt]");
        System.out.println("       genToken gen random salt [length] [-upper] [-*]");
        //System.out.println("       genToken access <id> <loginName> <password> <salt>");
        System.out.println("Param:");
        System.out.println("  ttl  support multi unit: m(minute) h(hour) d(day)");
        System.out.println("  e.g. genToken 1 admin 259200000  ## equals 72 hours");
        System.out.println("       genToken 1 admin 72h        ## means 72 hours");
    }

    public static String getRandomSalt(int length, int upperChars, int digitChars, int symbolChars) {
        String base = "abcdefghijklmnopqrstuvwxyz";
        String base09 = "0123456789";
        String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String BaSe = "~!@#$%.";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        length = length - upperChars - digitChars - symbolChars;
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }

        // insert with BASE
        for (int i = 0; i < upperChars; ++i) {
            int NUMBER = random.nextInt(BASE.length());
            int INDEX = random.nextInt(length+i);
            sb.insert(INDEX, BASE.charAt(NUMBER));
        }

        // digit
        length = length+upperChars;
        for (int i = 0; i < digitChars; ++i) {
            int NUMBER = random.nextInt(base09.length());

            int INDEX = random.nextInt(length+i);
                INDEX = Math.max(1, INDEX);
            sb.insert(INDEX, base09.charAt(NUMBER));
        }

        // insert with symbol
        length = length+digitChars;
        for (int i = 0; i < symbolChars; ++i) {
            int SYMBOL = random.nextInt(BaSe.length());

            int InDeX = random.nextInt(length+i);
                InDeX = Math.max(1, InDeX);
            sb.insert(InDeX, BaSe.charAt(SYMBOL));
        }

        return sb.toString();
    }

    public static String md5(String credentials, String saltSource) {
        ByteSource salt = new Md5Hash(saltSource);
        return (new SimpleHash("MD5", credentials, salt, 1024)).toString();
    }

    public static void main(String[] args) {
        if (args == null || args.length==0) {
            printUsage();
            return;
        }

        /**
         * gen random salt
         */
        if (args != null && (args.length == 3 || args.length ==4 || args.length==5 || args.length==6 || args.length==7) && (args[0]+args[1]+args[2]).compareTo("genrandomsalt") == 0) {
            try {
                int len = args.length >= 4 ? Integer.parseInt(args[3]) : 5;
                int LEN = args.length >= 5 ? Integer.parseInt(args[4]) : 0;
                int Len09 = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
                int LeN = args.length >= 7 ? Integer.parseInt(args[6]) : 0;

                String salt = getRandomSalt(len, LEN, Len09, LeN);
                System.out.println(salt);
                return;
            }catch (IllegalFormatException ex){
                System.out.println("length value invalid !");
            }
        }

        /**
         * gen password
         */
        if (args != null && (args.length == 3 || args.length ==4) && (args[0]+args[1]).compareTo("genpassword") == 0) {
            String password = args[2];
            String salt = args.length==4 ? args[3] : getRandomSalt(5, 0, 0, 0);
            if (password.length() == 0) {
                printUsage();
                return;
            }

            password = md5(password, salt);
            System.out.print(password + " ");
            System.out.println(salt);
            return;
        }

        if (args.length < 4 ) {
            printUsage();
            return;
        }

        if(args !=null && args.length==6 && args[0].equals("access")){
            Long orgId = Long.valueOf(args[1]);
            Long userId = Long.valueOf(args[2]);
            String account = args[3];
            String TTL = args[4];
            String token = genToken(orgId, userId, account, TTL);
            System.out.println(token);
            return;
        }

        /**
         * gen token
         */
        Long orgId = args[0].length() > 0 ? Long.valueOf(args[0]) : null;
        Long userId = Long.valueOf(args[1]);
        String account = args[2];
        String ttlString = args[3];
        String token = genToken(orgId, userId, account, ttlString);
        System.out.println(token);
    }

    private static String genToken(Long orgId, Long userId, String account, String ttlString){
        JWTToken genToken = new JWTToken();

        // get ttl
        Long ttl = 0L;
        try {
            ttl = Long.parseLong(ttlString);
        } catch (NumberFormatException e) {
            ttl = Long.valueOf(ttlString.substring(0, ttlString.length() - 1));
            String timeUnit = ttlString.substring(ttlString.length() - 1, ttlString.length());

            if (timeUnit.equals("s")) {
                ttl = ttl * 1000;
            } else if (timeUnit.equals("m")) {
                ttl = ttl * 60 * 1000;
            } else if (timeUnit.equals("h")) {
                ttl = ttl * 60 * 60 * 1000;
            } else if (timeUnit.equals("d")) {
                ttl = ttl * 24 * 60 * 60 * 1000;
            }
        }

        genToken.setTtl(ttl).setEncodedKey(encodedKey);
        // end expiredTimes

        return genToken.createToken(orgId, userId, account, 0, 1L, 0, "SYSTEM", 0L);
    }


    // TTL: ttl format 72h, 3d, etc
    private static String genAccessToken(Long orgId, Long userId, String account, String TTL){
        UserJWTToken genToken = new UserJWTToken();

        // get ttl
        Long ttl = 0L;
        try {
            ttl = Long.parseLong(TTL);
        } catch (NumberFormatException e) {
            ttl = Long.valueOf(TTL.substring(0, TTL.length() - 1));
            String timeUnit = TTL.substring(TTL.length() - 1, TTL.length());

            if (timeUnit.equals("s")) {
                ttl = ttl * 1000;
            } else if (timeUnit.equals("m")) {
                ttl = ttl * 60 * 1000;
            } else if (timeUnit.equals("h")) {
                ttl = ttl * 60 * 60 * 1000;
            } else if (timeUnit.equals("d")) {
                ttl = ttl * 24 * 60 * 60 * 1000;
            }
        }

        genToken.setTtl(ttl).setEncodedKey(accessEncodedKey);

        return genToken.createToken(orgId, userId, account);
    }
}
