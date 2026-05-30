package com.example.emotion_diary_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "quiz_template")
public class QuizTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(nullable = false, length = 500)
    private @Nullable String question;

    @Column(nullable = false, length = 20)
    private @Nullable String type;

    @Column(columnDefinition = "LONGTEXT")
    private @Nullable String options;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
