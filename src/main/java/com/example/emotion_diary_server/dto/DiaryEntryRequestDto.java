package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request body for creating or updating a diary entry.
 */
public record DiaryEntryRequestDto(
        /** Calendar date of the entry (one entry per owner per day). */
        LocalDate entryDate,
        /** Mood score on a 1–10 scale. */
        int moodScore,
        /** Optional free-text note. */
        @Nullable String textNote,
        /** Optional moodboard id linked to this entry. */
        @Nullable Long linkedMoodboardId,
        /** Optional local date-time for a reminder notification. */
        @Nullable LocalDateTime reminderAt
) {
}
