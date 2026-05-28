package com.example.emotion_diary_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
    private Long id;
    private String ownerUsername;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

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
