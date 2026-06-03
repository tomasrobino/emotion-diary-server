package com.example.emotion_diary_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single element placed on a moodboard canvas (text, image, or Fabric.js object).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodboardElementDto {

    /** Client-generated element identifier within the canvas. */
    private @Nullable String id;

    /** Element kind (e.g. {@code text}, {@code image}). */
    private @Nullable String type;

    /** Horizontal position as a fraction of canvas width (0–1). */
    private @Nullable Double x;

    /** Vertical position as a fraction of canvas height (0–1). */
    private @Nullable Double y;

    /** Width as a fraction of canvas width (0–1). */
    private @Nullable Double width;

    /** Height as a fraction of canvas height (0–1). */
    private @Nullable Double height;

    /** Stacking order; higher values render on top. */
    private @Nullable Integer zIndex;

    /** Rotation in degrees. */
    private @Nullable Double rotation;

    /** Text content for text elements. */
    private @Nullable String text;

    /** Font size in pixels for text elements. */
    private @Nullable Integer fontSize;

    /** CSS color string for text elements (e.g. {@code #ffffff}). */
    private @Nullable String color;

    /** Media asset id for image elements. */
    private @Nullable Long assetId;

    /** Raw Fabric.js JSON for complex shapes; used when {@link #type} requires it. */
    private @Nullable JsonNode fabricJson;
}
