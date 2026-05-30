package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.QuizTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizTemplateRepository extends JpaRepository<QuizTemplate, Long> {

    List<QuizTemplate> findAllByOrderBySortOrderAsc();
}
