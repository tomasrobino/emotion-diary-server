package com.example.emotion_diary_server.model;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Records that {@code likerUsername} liked the moodboard with {@code moodboardId}.
 */
@Getter
@Entity
@Table(
        name = "moodboard_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"moodboard_id", "liker_username"})
)
public class MoodboardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moodboard_id", nullable = false)
    private Long moodboardId;

    @Column(name = "liker_username", nullable = false)
    private String likerUsername;

    public MoodboardLike() {}

    public MoodboardLike(Long moodboardId, String likerUsername) {
        this.moodboardId = moodboardId;
        this.likerUsername = likerUsername;
    }
}
