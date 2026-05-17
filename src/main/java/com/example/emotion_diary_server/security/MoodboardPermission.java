package com.example.emotion_diary_server.security;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Represents an explicit permission grant:
 * "ownerUsername allows permittedUsername to access their moodboards."
 */
@Getter
@Entity
@Table(
        name = "moodboard_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_username", "permitted_username"})
)
public class MoodboardPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "permitted_username", nullable = false)
    private String permittedUsername;

    public MoodboardPermission() {}

    public MoodboardPermission(String ownerUsername, String permittedUsername) {
        this.ownerUsername = ownerUsername;
        this.permittedUsername = permittedUsername;
    }

}