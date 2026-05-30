package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.config.MoodboardProperties;
import com.example.emotion_diary_server.dto.MoodboardResponseDto;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class MoodboardNameService {

    private final int maxNameLength;

    public MoodboardNameService(MoodboardProperties properties) {
        this.maxNameLength = properties.maxNameLength();
    }

    /**
     * Normalizes an optional name for create: blank/missing becomes the default title.
     */
    public String normalizeForCreate(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return MoodboardResponseDto.DEFAULT_NAME;
        }
        return validateAndTrim(name);
    }

    /**
     * Validates a required name for rename (must be non-blank after trim).
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
