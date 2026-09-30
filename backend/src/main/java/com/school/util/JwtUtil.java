package com.school.util;

import com.school.common.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, Integer role, Integer accountLevel, Long schoolId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .claim("accountLevel", accountLevel)
                .claim("schoolId", schoolId)
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token；无效返回 null
     */
    public UserContext parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                    .parseClaimsJws(token).getBody();
            return new UserContext(
                    Long.valueOf(claims.getSubject()),
                    claims.get("role", Integer.class),
                    claims.get("accountLevel", Integer.class),
                    claims.get("schoolId", Long.class),
                    claims.get("username", String.class));
        } catch (Exception e) {
            return null;
        }
    }
}
