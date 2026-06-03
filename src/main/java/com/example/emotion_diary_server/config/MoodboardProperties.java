package com.example.emotion_diary_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Size and content limits for moodboards, bound from {@code moodboard.*} configuration.
 *
 * @param maxElements         maximum number of elements allowed in a moodboard
 * @param maxContentJsonBytes maximum serialized JSON size for moodboard content
 * @param maxNameLength       maximum length of the moodboard display name
 */
@ConfigurationProperties(prefix = "moodboard")
public record MoodboardProperties(
        int maxElements,
        int maxContentJsonBytes,
        int maxNameLength
) {
}
