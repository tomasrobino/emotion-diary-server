package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.MoodboardLike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodboardLikeRepository extends JpaRepository<MoodboardLike, Long> {

    boolean existsByMoodboardIdAndLikerUsername(Long moodboardId, String likerUsername);

    @Transactional
    void deleteByMoodboardIdAndLikerUsername(Long moodboardId, String likerUsername);

    @Transactional
    void deleteByMoodboardId(Long moodboardId);

    List<MoodboardLike> findByMoodboardId(Long moodboardId);

    List<MoodboardLike> findByLikerUsername(String likerUsername);

    long countByMoodboardId(Long moodboardId);
}
