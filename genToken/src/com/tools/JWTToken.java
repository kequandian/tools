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

    public JWTToken setTtlMillis(long ttl){
        this.ttl = ttl;
        return this;
    }
    public JWTToken setEncodedKey(String encodedKey){
        this.encodedKey = encodedKey;
        return this;
    }

    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    public String createToken(Long orgId, Long userId, String account,Integer userType, Long tenantOrgId, Integer devUserType, String bUserType, Long bUserId) {
        return createJWT(orgId, userId, account, userType, ttl, tenantOrgId, devUserType, bUserType, bUserId);
    }
    @Deprecated
    public String createToken(Long orgId, Long userId, String account,Integer userType, Long tenantOrgId, Integer devUserType, String bUserType) {
        return createJWT(orgId, userId, account, userType, ttl, tenantOrgId, devUserType, bUserType, null);
    }

    public String createUserToken(Long orgId, Long userId, String account, Long bUserId) {
        return createJWT(orgId, userId, account, null, ttl, orgId, null, null, bUserId);
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

    private String createJWT(Long orgId, Long userId, String account,Integer userType, long ttlMillis, Long tenantOrgId, Integer devUserType, String bUserType, Long bUserId) {
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
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
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
