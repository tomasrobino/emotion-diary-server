package com.example.emotion_diary_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodboardElementDto {
    private String id;
    private String type;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private Integer zIndex;
    private Double rotation;
    private String text;
    private Integer fontSize;
    private String color;
    private Long assetId;
    private JsonNode fabricJson;
}
