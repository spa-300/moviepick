
package com.moviepick.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey secretKey;

    // Access Token 유효시간: 30분
    private static final long ACCESS_TOKEN_EXPIRATION =
            30 * 60 * 1000L;

    // Refresh Token 유효시간: 14일
    private static final long REFRESH_TOKEN_EXPIRATION =
            14 * 24 * 60 * 60 * 1000L;

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    // Access Token 생성
    public String createAccessToken(Long memberId, String role) {

        Date now = new Date();

        return Jwts.builder()
                .subject(memberId.toString())
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(
                        now.getTime() + ACCESS_TOKEN_EXPIRATION
                ))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(Long memberId, long authVersion) {

        Date now = new Date();

        return Jwts.builder()
                .subject(memberId.toString())
                .id(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .claim("authVersion", authVersion)
                .issuedAt(now)
                .expiration(new Date(
                        now.getTime() + REFRESH_TOKEN_EXPIRATION
                ))
                .signWith(secretKey)
                .compact();
    }

    // JWT 서명 및 만료시간 검증
    public Claims validateToken(String token) {

        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "유효하지 않은 토큰입니다.", e
            );
        }
    }

    // Refresh Token 검증 후 회원 ID 추출
    public Long validateRefreshToken(String refreshToken) {

        Claims claims = validateToken(refreshToken);

        String tokenType = claims.get("type", String.class);

        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException(
                    "Refresh Token이 아닙니다."
            );
        }

        try {
            return Long.valueOf(claims.getSubject());

        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "토큰의 회원 ID가 올바르지 않습니다.", e
            );
        }
    }
}
