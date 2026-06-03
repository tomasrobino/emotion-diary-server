package com.example.emotion_diary_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialized moodboard canvas: version metadata, optional Fabric canvas JSON, and element list.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodboardContentDto {

    /** Schema version for forward-compatible content migrations. */
    private int version;

    /** Optional root Fabric.js canvas JSON. */
    private @Nullable JsonNode canvas;

    /** Ordered list of elements on the canvas. */
    private List<MoodboardElementDto> elements = new ArrayList<>();
}
