package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MoodboardRenameRequestDto {
    private @Nullable String name;
}
