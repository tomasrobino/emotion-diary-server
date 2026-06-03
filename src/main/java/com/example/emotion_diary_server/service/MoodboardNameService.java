package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.config.MoodboardProperties;
import com.example.emotion_diary_server.dto.MoodboardResponseDto;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * Normalizes and validates moodboard display names against configured length limits.
 */
@Service
public class MoodboardNameService {

    private final int maxNameLength;

    /**
     * @param properties moodboard configuration including max name length
     */
    public MoodboardNameService(MoodboardProperties properties) {
        this.maxNameLength = properties.maxNameLength();
    }

    /**
     * Normalizes an optional name for create: blank or missing becomes the default title.
     *
     * @param name raw name from the client, may be null or blank
     * @return trimmed name or {@link MoodboardResponseDto#DEFAULT_NAME}
     * @throws IllegalArgumentException if a non-blank name exceeds the maximum length
     */
    public String normalizeForCreate(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return MoodboardResponseDto.DEFAULT_NAME;
        }
        return validateAndTrim(name);
    }

    /**
     * Validates a required name for rename (must be non-blank after trim).
     *
     * @param name raw name from the client
     * @return trimmed name
     * @throws IllegalArgumentException if name is null, blank, or exceeds the maximum length
     */
    public String validateForRename(@Nullable String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del moodboard no puede estar vacío");
        }
        return validateAndTrim(name);
    }

    private String validateAndTrim(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("El nombre del moodboard no puede estar vacío");
        }
        if (trimmed.length() > maxNameLength) {
            throw new IllegalArgumentException(
                    "El nombre del moodboard no puede superar " + maxNameLength + " caracteres"
            );
        }
        return trimmed;
    }
}
