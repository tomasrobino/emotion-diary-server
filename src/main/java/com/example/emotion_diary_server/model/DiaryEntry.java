package com.example.emotion_diary_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "diary_entry",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_diary_entry_user_date",
                columnNames = {"owner_username", "entry_date"}
        )
)
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "owner_username", nullable = false)
    private @Nullable String ownerUsername;

    @Column(name = "entry_date", nullable = false)
    private @Nullable LocalDate entryDate;

    @Column(name = "mood_score", nullable = false)
    private int moodScore;

    @Column(name = "text_note", columnDefinition = "TEXT")
    private @Nullable String textNote;

    @Column(name = "linked_moodboard_id")
    private @Nullable Long linkedMoodboardId;

    @Column(name = "reminder_at")
    private @Nullable LocalDateTime reminderAt;

    @Column(name = "created_at", nullable = false)
    private @Nullable Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;
}
