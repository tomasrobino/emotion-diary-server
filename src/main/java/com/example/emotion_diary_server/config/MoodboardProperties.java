package com.example.emotion_diary_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "moodboard")
public record MoodboardProperties(
        int maxElements,
        int maxContentJsonBytes,
        int maxNameLength
) {
}
