package com.example.emotion_diary_server.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

/**
 * CORS allow-list settings, bound from {@code app.cors.*} configuration.
 * Origin values may be comma-separated lists in application properties.
 *
 * @param allowedOrigins         exact allowed origins, or {@code null} if unset
 * @param allowedOriginPatterns  origin patterns (e.g. {@code *}), or {@code null} if unset
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        @Nullable String allowedOrigins,
        @Nullable String allowedOriginPatterns
) {

    /**
     * @return allowed origins split from {@link #allowedOrigins}, or empty when unset
     */
    public String[] allowedOriginsArray() {
        return splitCsv(allowedOrigins);
    }

    /**
     * @return allowed origin patterns split from {@link #allowedOriginPatterns}, or empty when unset
     */
    public String[] allowedOriginPatternsArray() {
        return splitCsv(allowedOriginPatterns);
    }

    private static String[] splitCsv(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
