package com.soulmate.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类，提供Token生成、验证和解析功能
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:7200000}")
    private Long expiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(String userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
        
        log.debug("生成JWT Token: userId={}, role={}, expiresAt={}", userId, role, expiryDate);
        return token;
    }

    /**
     * 从Token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            log.warn("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (JwtException e) {
            log.warn("获取用户角色失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("检查Token过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 解析Token获取Claims
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取Token过期时间（秒）
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }
}

