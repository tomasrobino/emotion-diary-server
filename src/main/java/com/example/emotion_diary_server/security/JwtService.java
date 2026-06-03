package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Creates and validates JWT access tokens using the configured secret and expiration.
 */
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                Objects.requireNonNull(jwtProperties.secret()).getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Issues a new signed JWT for the given username.
     *
     * @param username subject (username) embedded in the token
     * @return compact JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Reads the subject (username) from a JWT.
     *
     * @param token compact JWT
     * @return username, or {@code null} if the subject is absent
     */
    public @Nullable String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Reads the unique token identifier (JTI) from a JWT.
     *
     * @param token compact JWT
     * @return JWT ID, or {@code null} if absent
     */
    public @Nullable String extractJti(String token) {
        return parseClaims(token).getId();
    }

    /**
     * Reads the expiration instant from a JWT.
     *
     * @param token compact JWT
     * @return expiration time
     */
    public Instant extractExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    /**
     * Checks whether the token matches the user, is not expired, and has a verifiable signature.
     *
     * @param token       compact JWT
     * @param userDetails expected principal
     * @return {@code true} if the token is valid for the user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
