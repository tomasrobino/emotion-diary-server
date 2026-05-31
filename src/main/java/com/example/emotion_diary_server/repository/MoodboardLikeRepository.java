package com.example.emotion_diary_server.repository;



import com.example.emotion_diary_server.model.MoodboardLike;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.EntityGraph;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.List;



public interface MoodboardLikeRepository extends JpaRepository<MoodboardLike, Long> {



    boolean existsByMoodboard_IdAndLiker_Username(Long moodboardId, String likerUsername);



    @Transactional

    void deleteByMoodboard_IdAndLiker_Username(Long moodboardId, String likerUsername);



    @Transactional

    void deleteByMoodboard_Id(Long moodboardId);



    List<MoodboardLike> findByMoodboard_Id(Long moodboardId);



    @EntityGraph(attributePaths = "moodboard")

    List<MoodboardLike> findByLiker_Username(String likerUsername);



    long countByMoodboard_Id(Long moodboardId);

    @Transactional
    void deleteByLiker_Username(String likerUsername);

}

