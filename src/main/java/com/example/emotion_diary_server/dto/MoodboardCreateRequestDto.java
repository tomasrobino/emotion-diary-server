package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MoodboardCreateRequestDto {
    private @Nullable String name;
    private @Nullable MoodboardContentDto content;
}
