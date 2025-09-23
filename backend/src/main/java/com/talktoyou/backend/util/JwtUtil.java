package com.talktoyou.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtUtil(
            @Value("${app.jwt.secret:mySecretKey1234567890123456789012345678901234567890123456789012345678901234567890abcdefghijklmnop}") String secret,
            @Value("${app.jwt.expiration:86400000}") long jwtExpiration) { // 24시간

        // HS512에 안전한 키 생성
        if (secret.getBytes().length < 64) {
            // 키가 64바이트 미만이면 안전한 키를 자동 생성
            this.secretKey = Jwts.SIG.HS512.key().build();
            log.warn("제공된 JWT secret이 너무 짧습니다. 안전한 키를 자동 생성했습니다.");
        } else {
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        }

        this.jwtExpiration = jwtExpiration;
    }

    // JWT 토큰 생성
    public String generateToken(String userId, String userName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId)
                .claim("userName", userName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // 토큰에서 사용자명 추출
    public String getUserNameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userName", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("잘못된 JWT 서명");
        } catch (MalformedJwtException ex) {
            log.error("잘못된 JWT 토큰");
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰");
        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 토큰");
        } catch (IllegalArgumentException ex) {
            log.error("JWT 클레임이 비어있음");
        }
        return false;
    }

    // 토큰 만료 시간 추출
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    // 토큰이 만료되었는지 확인
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}