package com.example.emotion_diary_server.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled job that purges expired revoked-token rows from the database.
 */
@Service
public class RevokedTokenCleanupService {

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenCleanupService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Deletes revoked-token records whose JWT expiration is in the past.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredTokens() {
        revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
