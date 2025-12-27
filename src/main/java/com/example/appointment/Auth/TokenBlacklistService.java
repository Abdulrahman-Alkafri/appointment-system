package com.example.appointment.Auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";
    private static final String USER_ACCESS_TOKENS_PREFIX = "user:access:";

    /**
     * Blacklist an access token with TTL
     * @param jti JWT ID of the token
     * @param ttlMillis Time to live in milliseconds
     */
    public void blacklistAccessToken(String jti, long ttlMillis) {
        if (jti == null || jti.isEmpty()) {
            log.warn("Attempted to blacklist access token with null/empty JTI");
            return;
        }

        if (ttlMillis <= 0) {
            log.debug("Access token already expired, skipping blacklist: {}", jti);
            return;
        }

        try {
            String key = ACCESS_BLACKLIST_PREFIX + jti;
            Map<String, Object> value = Map.of(
                    "blacklistedAt", System.currentTimeMillis(),
                    "type", "access"
            );
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("Blacklisted access token: {} with TTL: {}ms", jti, ttlMillis);
        } catch (Exception e) {
            log.error("Failed to blacklist access token: {}", jti, e);
        }
    }

    /**
     * Blacklist a refresh token with TTL
     * @param jti JWT ID of the refresh token
     * @param ttlMillis Time to live in milliseconds
     */
    public void blacklistRefreshToken(String jti, long ttlMillis) {
        if (jti == null || jti.isEmpty()) {
            log.warn("Attempted to blacklist refresh token with null/empty JTI");
            return;
        }

        if (ttlMillis <= 0) {
            log.debug("Refresh token already expired, skipping blacklist: {}", jti);
            return;
        }

        try {
            String key = REFRESH_BLACKLIST_PREFIX + jti;
            Map<String, Object> value = Map.of(
                    "blacklistedAt", System.currentTimeMillis(),
                    "type", "refresh"
            );
            redisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("Blacklisted refresh token: {} with TTL: {}ms", jti, ttlMillis);
        } catch (Exception e) {
            log.error("Failed to blacklist refresh token: {}", jti, e);
        }
    }

    /**
     * Check if an access token is blacklisted
     * @param jti JWT ID of the token
     * @return true if blacklisted, false otherwise
     */
    public boolean isAccessTokenBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        try {
            String key = ACCESS_BLACKLIST_PREFIX + jti;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check if access token is blacklisted: {}", jti, e);
            return false; // Fail-safe: allow authentication if Redis fails
        }
    }

    /**
     * Check if a refresh token is blacklisted
     * @param jti JWT ID of the refresh token
     * @return true if blacklisted, false otherwise
     */
    public boolean isRefreshTokenBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        try {
            String key = REFRESH_BLACKLIST_PREFIX + jti;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check if refresh token is blacklisted: {}", jti, e);
            return false; // Fail-safe: allow authentication if Redis fails
        }
    }

    /**
     * Track a user's active access token
     * @param userId User ID
     * @param jti JWT ID of the access token
     * @param ttlMillis Time to live in milliseconds
     */
    public void trackUserAccessToken(Long userId, String jti, long ttlMillis) {
        if (userId == null || jti == null || jti.isEmpty()) {
            log.warn("Invalid parameters for tracking user access token: userId={}, jti={}", userId, jti);
            return;
        }

        if (ttlMillis <= 0) {
            log.debug("Token already expired, skipping tracking: {}", jti);
            return;
        }

        try {
            String key = USER_ACCESS_TOKENS_PREFIX + userId;
            redisTemplate.opsForSet().add(key, jti);
            redisTemplate.expire(key, ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("Tracking access token {} for user {}", jti, userId);
        } catch (Exception e) {
            log.error("Failed to track access token for user {}: {}", userId, jti, e);
        }
    }

    /**
     * Remove a user's access token from tracking
     * @param userId User ID
     * @param jti JWT ID of the access token
     */
    public void removeUserAccessToken(Long userId, String jti) {
        if (userId == null || jti == null || jti.isEmpty()) {
            return;
        }

        try {
            String key = USER_ACCESS_TOKENS_PREFIX + userId;
            redisTemplate.opsForSet().remove(key, jti);
            log.debug("Removed access token {} from user {} tracking", jti, userId);
        } catch (Exception e) {
            log.error("Failed to remove access token from user {} tracking: {}", userId, jti, e);
        }
    }

    /**
     * Get all active access token JTIs for a user
     * @param userId User ID
     * @return Set of JTIs
     */
    public Set<String> getUserAccessTokens(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        try {
            String key = USER_ACCESS_TOKENS_PREFIX + userId;
            Set<Object> members = redisTemplate.opsForSet().members(key);
            if (members == null) {
                return Collections.emptySet();
            }

            Set<String> jtiSet = new HashSet<>();
            for (Object member : members) {
                if (member instanceof String) {
                    jtiSet.add((String) member);
                }
            }
            return jtiSet;
        } catch (Exception e) {
            log.error("Failed to get access tokens for user {}", userId, e);
            return Collections.emptySet();
        }
    }

    /**
     * Blacklist all access tokens for a user (used in logout all sessions)
     * @param userId User ID
     */
    public void blacklistAllUserAccessTokens(Long userId) {
        if (userId == null) {
            log.warn("Attempted to blacklist all tokens for null userId");
            return;
        }

        try {
            Set<String> tokens = getUserAccessTokens(userId);
            log.info("Blacklisting {} access tokens for user {}", tokens.size(), userId);

            for (String jti : tokens) {
                // Use a default TTL of 30 minutes for blacklisting
                // The actual token's remaining TTL should be used if available
                blacklistAccessToken(jti, TimeUnit.MINUTES.toMillis(30));
            }

            // Clear the tracking set
            String key = USER_ACCESS_TOKENS_PREFIX + userId;
            redisTemplate.delete(key);

            log.info("Successfully blacklisted all access tokens for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to blacklist all access tokens for user {}", userId, e);
        }
    }

    /**
     * Cleanup expired blacklist entries
     * @return Number of cleaned entries
     */
    public long cleanupExpiredBlacklistEntries() {
        long cleanedCount = 0;

        try {
            // Scan for access blacklist entries
            cleanedCount += cleanupKeysByPattern(ACCESS_BLACKLIST_PREFIX + "*");

            // Scan for refresh blacklist entries
            cleanedCount += cleanupKeysByPattern(REFRESH_BLACKLIST_PREFIX + "*");

            log.info("Cleanup completed. Total entries removed: {}", cleanedCount);
        } catch (Exception e) {
            log.error("Error during blacklist cleanup", e);
        }

        return cleanedCount;
    }

    /**
     * Cleanup keys matching a pattern
     * @param pattern Redis key pattern
     * @return Number of cleaned keys
     */
    private long cleanupKeysByPattern(String pattern) {
        long count = 0;

        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build();

            Cursor<String> cursor = redisTemplate.scan(options);

            while (cursor.hasNext()) {
                String key = cursor.next();
                Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);

                // Remove keys that have expired or have very short TTL (<1 second)
                if (ttl != null && ttl < 1000) {
                    redisTemplate.delete(key);
                    count++;
                }
            }

            cursor.close();
        } catch (Exception e) {
            log.error("Error cleaning up keys with pattern: {}", pattern, e);
        }

        return count;
    }
}
