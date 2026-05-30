package com.example.emotion_diary_server.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        @Nullable String allowedOrigins,
        @Nullable String allowedOriginPatterns
) {

    public String[] allowedOriginsArray() {
        return splitCsv(allowedOrigins);
    }

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
