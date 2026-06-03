package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.config.MoodboardProperties;
import com.example.emotion_diary_server.dto.MoodboardContentDto;
import com.example.emotion_diary_server.dto.MoodboardElementDto;
import com.example.emotion_diary_server.repository.MoodboardMediaRepository;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Serializes, deserializes, and validates moodboard canvas content against configured limits.
 */
@Service
public class MoodboardContentService {

    private static final Set<String> ELEMENT_TYPES = Set.of("text", "image", "fabric");
    private final ObjectMapper objectMapper;
    private final MoodboardMediaRepository mediaRepository;
    private final MoodboardProperties properties;

    /**
     * @param objectMapper    JSON mapper for content DTOs
     * @param mediaRepository used to verify image asset ownership on update
     * @param properties      size and element limits
     */
    public MoodboardContentService(
            ObjectMapper objectMapper,
            MoodboardMediaRepository mediaRepository,
            MoodboardProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.mediaRepository = mediaRepository;
        this.properties = properties;
    }

    /**
     * Converts content to JSON and enforces the maximum serialized size.
     *
     * @param content structured moodboard content
     * @return JSON string suitable for persistence
     * @throws IllegalArgumentException if serialization fails or JSON exceeds the configured byte limit
     */
    public String serialize(MoodboardContentDto content) {
        try {
            String json = objectMapper.writeValueAsString(content);
            if (json.length() > properties.maxContentJsonBytes()) {
                throw new IllegalArgumentException(
                        "Content JSON exceeds maximum size of " + properties.maxContentJsonBytes() + " bytes"
                );
            }
            return json;
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid moodboard content", e);
        }
    }

    /**
     * Parses persisted JSON into a content DTO.
     *
     * @param contentJson stored JSON, must be non-blank
     * @return parsed content
     * @throws IllegalArgumentException if JSON is missing, blank, or invalid
     */
    public MoodboardContentDto deserialize(@Nullable String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            throw new IllegalArgumentException("Moodboard content is required");
        }
        try {
            return objectMapper.readValue(contentJson, MoodboardContentDto.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Moodboard content is not valid JSON", e);
        }
    }

    /**
     * Validates content for an existing moodboard, including media asset ownership for image elements.
     *
     * @param content     content to validate
     * @param moodboardId id of the moodboard being updated
     * @throws IllegalArgumentException if content is invalid or references unknown assets
     */
    public void validate(@Nullable MoodboardContentDto content, Long moodboardId) {
        if (content == null) {
            throw new IllegalArgumentException("Moodboard content is required");
        }
        if (content.getVersion() != 1) {
            throw new IllegalArgumentException("Unsupported content version: " + content.getVersion());
        }
        if (content.getElements().size() > properties.maxElements()) {
            throw new IllegalArgumentException(
                    "Too many elements (max " + properties.maxElements() + ")"
            );
        }
        for (MoodboardElementDto element : content.getElements()) {
            validateElement(element, moodboardId, false);
        }
    }

    /**
     * Validates content for moodboard creation (image elements must not reference {@code assetId} yet).
     *
     * @param content content to validate
     * @throws IllegalArgumentException if content is invalid for create
     */
    public void validateForCreate(@Nullable MoodboardContentDto content) {
        if (content == null) {
            throw new IllegalArgumentException("Moodboard content is required");
        }
        if (content.getVersion() != 1) {
            throw new IllegalArgumentException("Unsupported content version: " + content.getVersion());
        }
        if (content.getElements().size() > properties.maxElements()) {
            throw new IllegalArgumentException(
                    "Too many elements (max " + properties.maxElements() + ")"
            );
        }
        for (MoodboardElementDto element : content.getElements()) {
            validateElement(element, null, true);
        }
    }

    private void validateElement(@Nullable MoodboardElementDto element, @Nullable Long moodboardId, boolean creating) {
        if (element == null) {
            throw new IllegalArgumentException("Element is required");
        }
        if (element.getType() == null || !ELEMENT_TYPES.contains(element.getType())) {
            throw new IllegalArgumentException("Unknown element type: " + element.getType());
        }
        if (element.getId() == null || element.getId().isBlank()) {
            throw new IllegalArgumentException("Element id is required");
        }
        switch (element.getType()) {
            case "text" -> {
                if (element.getText() == null) {
                    throw new IllegalArgumentException("Text element requires text");
                }
            }
            case "image" -> {
                if (creating) {
                    if (element.getAssetId() != null) {
                        throw new IllegalArgumentException(
                                "assetId references are not allowed when creating a moodboard; upload media after creation"
                        );
                    }
                } else {
                    if (element.getAssetId() == null) {
                        throw new IllegalArgumentException(element.getType() + " element requires assetId");
                    }
                    if (moodboardId != null
                            && !mediaRepository.existsByIdAndMoodboard_Id(element.getAssetId(), moodboardId)) {
                        throw new IllegalArgumentException(
                                "assetId " + element.getAssetId() + " does not belong to this moodboard"
                        );
                    }
                }
            }
            case "fabric" -> {
                if (element.getFabricJson() == null) {
                    throw new IllegalArgumentException("Fabric element requires fabricJson");
                }
            }
            default -> throw new IllegalArgumentException("Unknown element type: " + element.getType());
        }
    }
}
