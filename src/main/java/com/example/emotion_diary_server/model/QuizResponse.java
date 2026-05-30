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

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "quiz_response",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_quiz_response_user_date",
                columnNames = {"owner_username", "response_date"}
        )
)
public class QuizResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "owner_username", nullable = false)
    private @Nullable String ownerUsername;

    @Column(name = "response_date", nullable = false)
    private @Nullable LocalDate responseDate;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private @Nullable String answers;

    @Column(name = "created_at", nullable = false)
    private @Nullable Instant createdAt;
}
