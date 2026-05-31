package com.example.emotion_diary_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import org.jspecify.annotations.Nullable;
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
    private @Nullable Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "moodboard_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_media_moodboard")
    )
    private Moodboard moodboard;

    @Column(nullable = false)
    private String contentType;

    private @Nullable String originalFilename;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public MoodboardMedia(Moodboard moodboard, String contentType, @Nullable String originalFilename, byte[] data) {
        this.moodboard = moodboard;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
        this.data = data;
        this.sizeBytes = data.length;
    }

    public Long getMoodboardId() {
        return moodboard.getId();
    }
}
