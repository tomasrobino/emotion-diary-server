package com.example.emotion_diary_server.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

/**
 * Persistence access for {@link RevokedToken} records.
 */
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    /**
     * Removes revocation entries that are no longer needed because the JWT has expired.
     *
     * @param instant cutoff; entries expiring before this instant are deleted
     */
    void deleteByExpiresAtBefore(Instant instant);
}
