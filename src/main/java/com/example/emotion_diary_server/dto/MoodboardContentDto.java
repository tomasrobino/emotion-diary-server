package com.example.emotion_diary_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodboardContentDto {
    private int version;
    private JsonNode canvas;
    private List<MoodboardElementDto> elements = new ArrayList<>();
}
