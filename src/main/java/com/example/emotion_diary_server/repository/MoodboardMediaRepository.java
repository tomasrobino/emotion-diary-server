package com.example.emotion_diary_server.repository;



import com.example.emotion_diary_server.model.MoodboardMedia;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;



public interface MoodboardMediaRepository extends JpaRepository<MoodboardMedia, Long> {

    void deleteByMoodboard_Id(Long moodboardId);



    boolean existsByIdAndMoodboard_Id(Long id, Long moodboardId);



    Optional<MoodboardMedia> findByIdAndMoodboard_Id(Long id, Long moodboardId);

}

