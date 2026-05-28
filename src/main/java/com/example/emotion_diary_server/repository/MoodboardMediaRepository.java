package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.MoodboardMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoodboardMediaRepository extends JpaRepository<MoodboardMedia, Long> {
    void deleteByMoodboardId(Long moodboardId);

    boolean existsByIdAndMoodboardId(Long id, Long moodboardId);

    Optional<MoodboardMedia> findByIdAndMoodboardId(Long id, Long moodboardId);
}
