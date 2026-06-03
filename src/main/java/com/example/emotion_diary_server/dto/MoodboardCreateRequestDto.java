package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for creating a new moodboard.
 */
@Getter
@Setter
@NoArgsConstructor
public class MoodboardCreateRequestDto {

    /** Optional display name; defaults on the server when omitted. */
    private @Nullable String name;

    /** Initial canvas content including elements and version. */
    private @Nullable MoodboardContentDto content;
}
