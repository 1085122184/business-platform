package com.cjx.common.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * 与Hutool区别:
 * - Hutool的JWTUtil较为基础
 * - 本类提供完整的Token生成、解析、刷新功能
 *
 * @author system
 */
@Slf4j
public class JwtUtil {
    /** 密钥(生产环境应该放在配置文件中) */
    private static final String SECRET_KEY = "your-256-bit-secret-key-here-must-be-long-enough-for-hs256";

    /** Token有效期(7天) */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /** Token刷新期(3天内可刷新) */
    private static final long REFRESH_TIME = 3 * 24 * 60 * 60 * 1000L;

    /**
     * 生成密钥
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     * @param userId 用户ID
     * @param username 用户名
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从Token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断Token是否需要刷新
     * 距离过期时间小于3天时需要刷新
     */
    public static boolean needRefresh(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        Date expiration = claims.getExpiration();
        long remainTime = expiration.getTime() - System.currentTimeMillis();
        return remainTime > 0 && remainTime < REFRESH_TIME;
    }

    /**
     * 刷新Token
     */
    public static String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }

        Long userId = getUserIdFromToken(token);
        String username = getUsernameFromToken(token);

        return generateToken(userId, username);
    }

    /**
     * 从Token中获取过期时间
     */
    public static Date getExpirationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}
