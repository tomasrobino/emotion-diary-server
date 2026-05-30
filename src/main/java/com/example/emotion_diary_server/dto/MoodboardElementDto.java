package com.example.emotion_diary_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodboardElementDto {
    private @Nullable String id;
    private @Nullable String type;
    private @Nullable Double x;
    private @Nullable Double y;
    private @Nullable Double width;
    private @Nullable Double height;
    private @Nullable Integer zIndex;
    private @Nullable Double rotation;
    private @Nullable String text;
    private @Nullable Integer fontSize;
    private @Nullable String color;
    private @Nullable Long assetId;
    private @Nullable JsonNode fabricJson;
}
