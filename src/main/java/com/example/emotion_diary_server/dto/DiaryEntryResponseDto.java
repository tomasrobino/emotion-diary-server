package com.example.emotion_diary_server.dto;

import com.example.emotion_diary_server.model.DiaryEntry;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryEntryResponseDto(
        long id,
        String ownerUsername,
        LocalDate entryDate,
        int moodScore,
        @Nullable String textNote,
        @Nullable Long linkedMoodboardId,
        @Nullable LocalDateTime reminderAt,
        Instant createdAt,
        Instant updatedAt
) {
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
