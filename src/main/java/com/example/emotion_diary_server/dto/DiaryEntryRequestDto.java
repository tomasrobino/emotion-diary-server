package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryEntryRequestDto(
        LocalDate entryDate,
        int moodScore,
        @Nullable String textNote,
        @Nullable Long linkedMoodboardId,
        @Nullable LocalDateTime reminderAt
) {
}
