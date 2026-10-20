
package com.moviepick.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    // Refresh Token 유효기간: 14일
    private static final Duration REFRESH_TOKEN_TTL =
            Duration.ofDays(14);

    // Redis에 Refresh Token 저장
    public void save(Long memberId, String refreshToken) {

        String key = createKey(refreshToken);

        redisTemplate.opsForValue().set(
                key,
                memberId.toString(),
                REFRESH_TOKEN_TTL
        );
    }

    // Refresh Token 유효성 확인
    public boolean isValid(Long memberId, String refreshToken) {

        String key = createKey(refreshToken);

        String savedMemberId =
                redisTemplate.opsForValue().get(key);

        return memberId.toString().equals(savedMemberId);
    }

    // Refresh Token 삭제
    public void delete(String refreshToken) {

        String key = createKey(refreshToken);

        redisTemplate.delete(key);
    }

    // Refresh Token을 SHA-256으로 해싱하여 Redis Key 생성
    private String createKey(String refreshToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    refreshToken.getBytes(StandardCharsets.UTF_8)
            );

            return "refresh:" + HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.", e
            );
        }
    }
}
