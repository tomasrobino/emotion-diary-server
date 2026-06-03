package com.example.emotion_diary_server.model;

import com.example.emotion_diary_server.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single diary entry for one calendar day per owner.
 * At most one entry may exist per owner and {@link #entryDate}.
 */
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

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    /** User who owns this entry. */
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "owner_username",
            referencedColumnName = "username",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_diary_owner")
    )
    private User owner;

    /** Calendar day this entry describes (unique per owner). */
    @Column(name = "entry_date", nullable = false)
    private @Nullable LocalDate entryDate;

    /** Mood rating for the day (application-defined scale). */
    @Column(name = "mood_score", nullable = false)
    private int moodScore;

    /** Optional free-text note. */
    @Column(name = "text_note", columnDefinition = "TEXT")
    private @Nullable String textNote;

    /** Optional moodboard linked to this entry. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "linked_moodboard_id",
            foreignKey = @ForeignKey(name = "fk_diary_linked_moodboard")
    )
    private @Nullable Moodboard linkedMoodboard;

    /** When a reminder should fire, if scheduled. */
    @Column(name = "reminder_at")
    private @Nullable LocalDateTime reminderAt;

    /** Timestamp when the row was first persisted. */
    @Column(name = "created_at", nullable = false)
    private @Nullable Instant createdAt;

    /** Timestamp of the last update to this row. */
    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;

    /**
     * @return owner username, or {@code null} if {@link #owner} is not loaded
     */
    public @Nullable String getOwnerUsername() {
        return owner != null ? owner.getUsername() : null;
    }

    /**
     * @return linked moodboard id, or {@code null} if none or not loaded
     */
    public @Nullable Long getLinkedMoodboardId() {
        return linkedMoodboard != null ? linkedMoodboard.getId() : null;
    }
}
