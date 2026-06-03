package com.example.emotion_diary_server.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT signing settings, bound from {@code jwt.*} configuration.
 *
 * @param secret        HMAC secret; required in production
 * @param expirationMs  token lifetime in milliseconds
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @Nullable String secret,
        long expirationMs
) {
}
