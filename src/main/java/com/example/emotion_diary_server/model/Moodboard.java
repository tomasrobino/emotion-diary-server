package com.example.emotion_diary_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.jspecify.annotations.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Moodboard {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private @Nullable Long id;
    private @Nullable String ownerUsername;

    @Column(columnDefinition = "LONGTEXT")
    private @Nullable String content;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(length = 100)
    private @Nullable String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private @Nullable byte[] thumbnail;

    public Moodboard(String ownerUsername, String content) {
        this.ownerUsername = ownerUsername;
        this.content = content;
    }

    public Moodboard(Long id, String ownerUsername, String content) {
        this.ownerUsername = ownerUsername;
        this.content = content;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Moodboard{id=" + id + ", owner=" + ownerUsername + "}";
    }
}
