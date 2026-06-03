package com.example.emotion_diary_server.repository;



import com.example.emotion_diary_server.model.MoodboardMedia;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;



/**
 * Persistence access for {@link MoodboardMedia} attachments.
 */
public interface MoodboardMediaRepository extends JpaRepository<MoodboardMedia, Long> {

    /**
     * Removes all media rows for a moodboard.
     *
     * @param moodboardId parent moodboard primary key
     */
    void deleteByMoodboard_Id(Long moodboardId);



    /**
     * @param id          media primary key
     * @param moodboardId expected parent moodboard id
     * @return {@code true} if a row exists with both ids
     */
    boolean existsByIdAndMoodboard_Id(Long id, Long moodboardId);



    /**
     * @param id          media primary key
     * @param moodboardId parent moodboard primary key
     * @return the media row when it belongs to the given moodboard
     */
    Optional<MoodboardMedia> findByIdAndMoodboard_Id(Long id, Long moodboardId);

}

