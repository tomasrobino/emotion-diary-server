package com.example.emotion_diary_server.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @Nullable String secret,
        long expirationMs
) {
}
