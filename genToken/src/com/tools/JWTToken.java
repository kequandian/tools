package com.tools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

public class JWTToken {
    private long ttl;
    private String encodedKey;
    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    public JWTToken setTtl(long ttl){
        this.ttl = ttl;
        return this;
    }
    public JWTToken setEncodedKey(String encodedKey){
        this.encodedKey = encodedKey;
        return this;
    }


    public String createToken(Long orgId, Long userId, String account,Integer userType, Long tenantOrgId, Integer devUserType, String bUserType, Long bUserId) {
        return createJWT(orgId, userId, account, userType, tenantOrgId, devUserType, bUserType, bUserId, ttl);
    }
    @Deprecated
    public String createToken(Long orgId, Long userId, String account, Integer userType, Long tenantOrgId, Integer devUserType, String bUserType) {
        return createJWT(orgId, userId, account, userType, tenantOrgId, devUserType, bUserType, null, ttl);
    }

    public String createUserToken(Long orgId, Long userId, String account, Long bUserId) {
        return createJWT(orgId, userId, account, null, orgId, null, null, bUserId, ttl);
    }

    public Claims parseToken(String token) {
        try {
            Claims claims = parseJWT(token);
            return claims;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    // private String createJWT(Long orgId, Long userId, String account, Long ttl) {
    //     long nowMillis = System.currentTimeMillis();
    //     Date now = new Date(nowMillis);
    //     JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT");
    //     if (orgId != null) {
    //         builder.claim("orgId", orgId + "");
    //     }
    //     builder.claim("userId", userId + "")
    //             .claim("account", account)
    //             .setIssuedAt(now)
    //             .setId(userId.toString())
    //             .setSubject(account)
    //             .signWith(getSignatureAlgorithm(), getSecretKey());
    //     if (ttl >= 0L) {
    //         long expMillis = nowMillis + ttl;
    //         Date exp = new Date(expMillis);
    //         builder.setExpiration(exp);
    //     }
    //     return builder.compact();
    // }


    // bUserType: SYSTEM
    // userType: 0-平台用户  1-组织管理人 2-个人用户 3-个人组织用户 100-管理员（租户管理员 自动跳过权限检查） 102-观察者用户 所有看的权限
    private String createJWT(Long orgId, Long userId, String account, Long ttl) {
        return this.createJWT(1L, userId, account, 
                              0,  // userType=0, 平台用户
                              1L,  // 租户组织ID=1L,
                              0,   // 开发用户 0-正常用户 1-测试用户 2-开发用户 3-运维用户
                              "SYSTEM",
                              0L,  // uUserId
                              ttl);
    }

    private String createJWT(Long orgId, Long userId, String account, Integer userType, Long tenantOrgId, Integer devUserType, String bUserType, Long bUserId, Long ttl) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("orgId", orgId + "")
                .claim("userId", userId + "")
                .claim("tenantOrgId", tenantOrgId)
                .claim("account", account)
                .claim("userType", userType)
                .claim("devUserType",devUserType)  // [1=Tester, 2=developer, 3=operator]
                .claim("bUserType", bUserType)
                .claim("bUserId", bUserId)
                .setIssuedAt(now)
                .setId(userId.toString())
                .setSubject(account)
                .signWith(getSignatureAlgorithm(), getSecretKey());
        if (ttl >= 0) {
            long expMillis = nowMillis + ttl;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    private Claims parseJWT(String jwt) throws Exception{
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(jwt).getBody();
        return claims;
    }

    private SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    private Key deserializeKey(String encodedKey) {
        //byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        byte[] decodedKey = Base64.getMimeDecoder().decode(encodedKey);
        Key key = new SecretKeySpec(decodedKey, getSignatureAlgorithm().getJcaName());
        return key;
    }

    private Key getSecretKey() {
        return deserializeKey(encodedKey);
    }

    private String serializeKey(Key key) {
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        return encodedKey;
    }
}
