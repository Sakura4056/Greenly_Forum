package com.plant.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // 配置文件中建议配置为 86400（秒），表示1天有效期

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 修复点1：将expiration（秒）转换为毫秒，保证Token有效期正确
        long expireTime = System.currentTimeMillis() + expiration * 1000;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(expireTime)) // 使用转换后的毫秒级过期时间
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 修复点2：单独捕获Token过期异常，便于定位问题
            log.error("JWT Token 已过期: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.error("JWT Token 格式错误: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT Token 签名错误: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.error("无效的 JWT Token: {}", e.getMessage());
            return null;
        }
    }

    // 修复点3：增强验证逻辑，主动校验Token是否过期（双重保障）
    public boolean validateToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        // 主动校验Token是否过期（即使parseClaimsJws已校验，这里做双重保障）
        return !claims.getExpiration().before(new Date());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    // 新增：获取Token过期时间，便于前端排查
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}