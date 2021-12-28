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
    public static final String PREFIX = "jwt";
    private static final String encodedKey = "L7A/6zARSkK1j7Vd5SDD9pSSqZlqF7mAhiOgRbgv9Smce6tf4cJnvKOjtKPxNNnWQj+2lQEScm3XIUjhW+YVZg==";
    private long ttlMillis = 2592000000L;  //30 days
    private String tokenType = "Bearer";
    private Boolean enableAttemptLogin = true;
    private Boolean nonShiroPermissionCheck = false;

    private static SignatureAlgorithm signatureAlgorithm;

    public GenToken() {
        signatureAlgorithm = SignatureAlgorithm.HS512;
    }

    public String createToken(Long orgId, Long userId, String account, Long expireTimes) {
        return this.createJWT(orgId, userId, account, expireTimes);
    }

    public Claims parseToken(String token) {
        try {
            Claims claims = this.parseJWT(token);
            return claims;
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    private String createJWT(Long orgId, Long userId, String account, Long expireTimes) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT");
        if (orgId != null) {
            builder.claim("orgId", orgId + "");
        }
        builder.claim("userId", userId + "")
                .claim("account", account)
                .setIssuedAt(now)
                .setId(userId.toString())
                .setSubject(account)
                .signWith(getSignatureAlgorithm(), getSecretKey());
        if (expireTimes >= 0L) {
            long expMillis = nowMillis + expireTimes;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    private Claims parseJWT(String jwt) throws Exception {
        Claims claims = (Claims) Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(jwt).getBody();
        return claims;
    }

    private SignatureAlgorithm getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    private Key deserializeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        Key key = new SecretKeySpec(decodedKey, getSignatureAlgorithm().getJcaName());
        return key;
    }

    private Key getSecretKey() {
        return this.deserializeKey(encodedKey);
    }

    private String serializeKey(Key key) {
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        return encodedKey;
    }

    public static void printUsageAndExit() {
        System.out.println("Usage: genToken <orgId> <userId> <account> <expireTimes>");
        System.out.println("       genToken gen password <password> [salt]");
        System.out.println("       genToken gen random salt [length] [-upper] [-*]");
        //System.out.println("       genToken access <id> <loginName> <password> <salt>");
//        System.out.println("Param:");
//        System.out.println("expireTimes  --Milliseconds, support unit are s:second m:minute h:hour d:day");
//        System.out.println("e.g. genToken 1 admin 259200000  ## equals 72 hours");
//        System.out.println("     genToken 1 admin 72h        ## means 72 hours");
//        System.out.println("     genToken gen password 123456 834232");
//        System.exit(1);
    }

    public static String getRandomSalt(int length, int upperChars, int symbolChars) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String BaSe = "~!@#$%._";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        length = length - upperChars - symbolChars;
        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }

        // insert with BASE
        for (int i = 0; i < upperChars; ++i) {
            int NUMBER = random.nextInt(BASE.length());
            int INDEX = random.nextInt(length);
            sb.insert(INDEX, BASE.charAt(NUMBER));
        }

        // insert with symbol
        for (int i = 0; i < symbolChars; ++i) {
            int NuMbeR = random.nextInt(BaSe.length());
            int InDeX = random.nextInt(length);
            sb.insert(InDeX, BaSe.charAt(NuMbeR));
        }

        return sb.toString();
    }

    public static String md5(String credentials, String saltSource) {
        ByteSource salt = new Md5Hash(saltSource);
        return (new SimpleHash("MD5", credentials, salt, 1024)).toString();
    }

    public static void main(String[] args) {
        if (args == null || args.length==0) {
            printUsageAndExit();
            return;
        }

        /**
         * gen random salt
         */
        if (args != null && (args.length == 3 || args.length ==4 || args.length==5 || args.length==6) && (args[0]+args[1]+args[2]).compareTo("genrandomsalt") == 0) {
            try {
                int len = args.length == 3 ? 5 : Integer.parseInt(args[3]);
                int LEN = args.length == 5 ? Integer.parseInt(args[4]) : 0;
                int LeN = args.length == 6 ? Integer.parseInt(args[5]) : 0;

                String salt = getRandomSalt(len, LEN, LeN);
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
            String salt = args.length==4 ? args[3] : getRandomSalt(5, 0, 0);
            if (password.length() == 0) {
                printUsageAndExit();
                return;
            }

            password = md5(password, salt);
            System.out.print(password + " ");
            System.out.println(salt);
            return;
        }

        if (args.length < 4 ) {
            printUsageAndExit();
            return;
        }


        /**
         * gen token
         */
        JWTToken genToken = new JWTToken();

        Long orgId = args[0].length() > 0 ? Long.valueOf(args[0]) : null;
        Long userId = Long.valueOf(args[1]);
        String account = args[2];
        String expireTimesString = args[3];

        // get expireTimes
        Long expireTimes = 0L;
        try {
            expireTimes = Long.parseLong(expireTimesString);
        } catch (NumberFormatException e) {
            expireTimes = Long.valueOf(args[3].substring(0, args[3].length() - 1));
            String timeUnit = args[3].substring(args[3].length() - 1, args[3].length());

            if (timeUnit.equals("s")) {
                expireTimes = expireTimes * 1000;
            } else if (timeUnit.equals("m")) {
                expireTimes = expireTimes * 60 * 1000;
            } else if (timeUnit.equals("h")) {
                expireTimes = expireTimes * 60 * 60 * 1000;
            } else if (timeUnit.equals("d")) {
                expireTimes = expireTimes * 24 * 60 * 60 * 1000;
            }
        }

        genToken.setTtlMillis(expireTimes).setEncodedKey(encodedKey);
        // end expiredTimes

        try {
            String token = genToken.createToken(orgId, userId, account, 0, 1L, 0, "SYSTEM", 0L);
            System.out.println(token);
            //System.out.println("token:" + genToken.parseToken(token));
        } catch (NumberFormatException nfe) {
            System.exit(1);
        }
        System.exit(0);
    }
}
