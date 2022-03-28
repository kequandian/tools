package com.tools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.ByteSource;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.sql.Date;
import java.util.Base64;
import java.util.IllegalFormatException;
import java.util.Random;

public class UserJWTToken {
    private long ttl;
    private String encodedKey;
    private static SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    public UserJWTToken setTtl(long ttl){
        this.ttl = ttl;
        return this;
    }
    public UserJWTToken setEncodedKey(String encodedKey){
        this.encodedKey = encodedKey;
        return this;
    }

    public String createToken(Long orgId, Long userId, String account) {
        return this.createJWT(orgId, userId, account, null);
    }

    /**
     * @param domainUserId 业务层用户ID
     * @return
     */
    public String createToken(Long orgId, Long userId, String account, Long domainUserId) {
        return this.createJWT(orgId, userId, account, domainUserId);
    }
    // end claim for domainUserId

    public Claims parseToken(String token) {
        try {
            return this.parseJWT(token);
        } catch (Exception ex) {
//            ex.printStackTrace();
//            log.warn("current token not match the signature key, go ahead next chain filter ..");
            return null;
        }
    }

    private String createJWT(Long orgId, Long userId, String account, Long domainUserId) {
        Assert.isTrue(userId!=null, "user.id is null !");
        Assert.isTrue(account!=null, "user.account is null !");

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder().setHeaderParam("type", "JWT")
                .claim("orgId", orgId==null?"":orgId)
                .claim("userId", userId)
                .claim("account", account)
                .claim("domainUserId", domainUserId==null?"":domainUserId)
                .setIssuedAt(now)
                .setId(userId.toString())
                .setSubject(account)
                .signWith(this.getSignatureAlgorithm(), this.getSecretKey());

        if (ttl >= 0L) {
            long expMillis = nowMillis + ttl;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        return builder.compact();
    }

    private Claims parseJWT(String jwt) {
        return (Claims)Jwts.parser().setSigningKey(this.getSecretKey()).parseClaimsJws(jwt).getBody();
    }

    private SignatureAlgorithm getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    private Key deserializeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, this.getSignatureAlgorithm().getJcaName());
    }

    private Key getSecretKey() {
        return this.deserializeKey(encodedKey);
    }

    private String serializeKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
