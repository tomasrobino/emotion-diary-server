package com.example.emotion_diary_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class MoodboardMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long moodboardId;

    @Column(nullable = false)
    private String contentType;

    private String originalFilename;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public MoodboardMedia(Long moodboardId, String contentType, String originalFilename, byte[] data) {
        this.moodboardId = moodboardId;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
        this.data = data;
        this.sizeBytes = data.length;
    }
}
