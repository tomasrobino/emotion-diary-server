package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.DiaryEntry;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Diary entry returned by the API.
 */
public record DiaryEntryResponseDto(
        /** Unique entry identifier. */
        long id,
        /** Username of the entry owner. */
        String ownerUsername,
        /** Calendar date of the entry. */
        LocalDate entryDate,
        /** Mood score on a 1–10 scale. */
        int moodScore,
        /** Optional free-text note. */
        @Nullable String textNote,
        /** Optional linked moodboard id. */
        @Nullable Long linkedMoodboardId,
        /** Optional reminder date-time in the owner's local timezone. */
        @Nullable LocalDateTime reminderAt,
        /** UTC instant when the entry was created. */
        Instant createdAt,
        /** UTC instant when the entry was last updated. */
        Instant updatedAt
) {
    /**
     * Maps a persisted {@link DiaryEntry} to an API response DTO.
     *
     * @param entry diary entry entity
     * @return response DTO with safe defaults for nullable entity fields
     */
    public static DiaryEntryResponseDto from(DiaryEntry entry) {
        return new DiaryEntryResponseDto(
                entry.getId() != null ? entry.getId() : 0L,
                entry.getOwnerUsername() != null ? entry.getOwnerUsername() : "",
                entry.getEntryDate() != null ? entry.getEntryDate() : LocalDate.now(),
                entry.getMoodScore(),
                entry.getTextNote(),
                entry.getLinkedMoodboardId(),
                entry.getReminderAt(),
                entry.getCreatedAt() != null ? entry.getCreatedAt() : Instant.now(),
                entry.getUpdatedAt() != null ? entry.getUpdatedAt() : Instant.now()
        );
    }
}
