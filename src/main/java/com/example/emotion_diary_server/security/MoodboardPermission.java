package com.example.emotion_diary_server.security;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import lombok.Getter;

/**
 * Represents an explicit permission grant:
 * "permittedUsername is allowed to access moodboard with moodboardId."
 */
@Getter
@Entity
@Table(
        name = "moodboard_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"moodboard_id", "permitted_username"})
)
public class MoodboardPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "moodboard_id", nullable = false)
    private Long moodboardId;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "permitted_username", nullable = false)
    private String permittedUsername;

    public MoodboardPermission() {}

    public MoodboardPermission(Long moodboardId, String ownerUsername, String permittedUsername) {
        this.moodboardId = moodboardId;
        this.ownerUsername = ownerUsername;
        this.permittedUsername = permittedUsername;
    }

}