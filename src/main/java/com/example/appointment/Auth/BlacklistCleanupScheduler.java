package com.example.appointment.Auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlacklistCleanupScheduler {

    private final TokenBlacklistService tokenBlacklistService;
    @Scheduled(cron = "0 */10 * * * *")
    public void cleanupExpiredBlacklistEntries() {
        log.info("Starting scheduled blacklist cleanup task");

        try {
            long cleanedCount = tokenBlacklistService.cleanupExpiredBlacklistEntries();
            log.info("Blacklist cleanup completed. Removed {} expired entries", cleanedCount);
        } catch (Exception e) {
            log.error("Error during blacklist cleanup", e);
        }
    }
}
