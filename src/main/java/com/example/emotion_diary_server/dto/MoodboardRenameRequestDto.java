package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for renaming a moodboard.
 */
@Getter
@Setter
@NoArgsConstructor
public class MoodboardRenameRequestDto {

    /** New display name; blank values may be rejected by validation. */
    private @Nullable String name;
}
