package com.talktoyou.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // 토큰을 블랙리스트에 추가
    public void addToBlacklist(String token, Date expirationDate) {
        try {
            String key = BLACKLIST_PREFIX + token;
            long ttl = expirationDate.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                log.debug("토큰이 블랙리스트에 추가되었습니다: {}", token.substring(0, Math.min(20, token.length())));
            }
        } catch (Exception e) {
            log.error("토큰을 블랙리스트에 추가하는 중 오류 발생", e);
        }
    }

    // 토큰이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("블랙리스트 확인 중 오류 발생", e);
            return false; // Redis 연결 오류 시 false 반환
        }
    }

    // 블랙리스트에서 토큰 제거 (필요한 경우)
    public void removeFromBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            log.debug("토큰이 블랙리스트에서 제거되었습니다");
        } catch (Exception e) {
            log.error("블랙리스트에서 토큰 제거 중 오류 발생", e);
        }
    }
}