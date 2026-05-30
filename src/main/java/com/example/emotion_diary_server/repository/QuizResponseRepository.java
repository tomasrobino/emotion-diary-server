package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {

    Optional<QuizResponse> findByOwnerUsernameAndResponseDate(String ownerUsername, LocalDate responseDate);
}
