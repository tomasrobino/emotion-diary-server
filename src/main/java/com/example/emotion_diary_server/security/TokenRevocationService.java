package com.example.emotion_diary_server.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Persists and checks revoked JWTs by JTI so logged-out tokens cannot be reused.
 */
@Service
public class TokenRevocationService {

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(JwtService jwtService, RevokedTokenRepository revokedTokenRepository) {
        this.jwtService = jwtService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Records a JWT as revoked until its natural expiration.
     *
     * @param token compact JWT to revoke
     */
    @Transactional
    public void revoke(String token) {
        String jti = jwtService.extractJti(token);
        if (jti == null) {
            return;
        }
        if (revokedTokenRepository.existsById(jti)) {
            return;
        }
        Instant expiresAt = jwtService.extractExpiration(token);
        revokedTokenRepository.save(new RevokedToken(jti, expiresAt));
    }

    /**
     * Determines whether the JWT has been revoked.
     *
     * @param token compact JWT
     * @return {@code true} if the token JTI is stored as revoked
     */
    public boolean isRevoked(String token) {
        String jti = jwtService.extractJti(token);
        return jti != null && revokedTokenRepository.existsById(jti);
    }
}
