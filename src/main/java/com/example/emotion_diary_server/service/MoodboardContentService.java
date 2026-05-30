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

@Service
public class MoodboardContentService {

    private static final Set<String> ELEMENT_TYPES = Set.of("text", "image", "fabric");
    private final ObjectMapper objectMapper;
    private final MoodboardMediaRepository mediaRepository;
    private final MoodboardProperties properties;

    public MoodboardContentService(
            ObjectMapper objectMapper,
            MoodboardMediaRepository mediaRepository,
            MoodboardProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.mediaRepository = mediaRepository;
        this.properties = properties;
    }

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
                            && !mediaRepository.existsByIdAndMoodboardId(element.getAssetId(), moodboardId)) {
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
