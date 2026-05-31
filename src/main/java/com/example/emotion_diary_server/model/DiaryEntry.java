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

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "owner_username",
            referencedColumnName = "username",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_diary_owner")
    )
    private User owner;

    @Column(name = "entry_date", nullable = false)
    private @Nullable LocalDate entryDate;

    @Column(name = "mood_score", nullable = false)
    private int moodScore;

    @Column(name = "text_note", columnDefinition = "TEXT")
    private @Nullable String textNote;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "linked_moodboard_id",
            foreignKey = @ForeignKey(name = "fk_diary_linked_moodboard")
    )
    private @Nullable Moodboard linkedMoodboard;

    @Column(name = "reminder_at")
    private @Nullable LocalDateTime reminderAt;

    @Column(name = "created_at", nullable = false)
    private @Nullable Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;

    public @Nullable String getOwnerUsername() {
        return owner != null ? owner.getUsername() : null;
    }

    public @Nullable Long getLinkedMoodboardId() {
        return linkedMoodboard != null ? linkedMoodboard.getId() : null;
    }
}
