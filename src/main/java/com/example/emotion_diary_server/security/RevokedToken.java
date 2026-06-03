package com.example.emotion_diary_server.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity storing a revoked JWT identifier (JTI) and its expiration for cleanup.
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RevokedToken {

    @Id
    private String jti;

    private Instant expiresAt;

    public RevokedToken(String jti, Instant expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }
}
