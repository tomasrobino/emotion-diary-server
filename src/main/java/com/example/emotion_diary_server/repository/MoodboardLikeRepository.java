package com.example.emotion_diary_server.repository;



import com.example.emotion_diary_server.model.MoodboardLike;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.EntityGraph;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.List;



/**
 * Persistence access for {@link MoodboardLike} rows.
 */
public interface MoodboardLikeRepository extends JpaRepository<MoodboardLike, Long> {



    /**
     * @param moodboardId   moodboard primary key
     * @param likerUsername username of the potential liker
     * @return {@code true} if that user already liked the moodboard
     */
    boolean existsByMoodboard_IdAndLiker_Username(Long moodboardId, String likerUsername);



    /**
     * Removes a single like from a user on a moodboard.
     *
     * @param moodboardId   moodboard primary key
     * @param likerUsername username of the liker
     */
    @Transactional

    void deleteByMoodboard_IdAndLiker_Username(Long moodboardId, String likerUsername);



    /**
     * Removes all likes on a moodboard (e.g. when the moodboard is deleted).
     *
     * @param moodboardId moodboard primary key
     */
    @Transactional

    void deleteByMoodboard_Id(Long moodboardId);



    /**
     * @param moodboardId moodboard primary key
     * @return all likes for that moodboard
     */
    List<MoodboardLike> findByMoodboard_Id(Long moodboardId);



    /**
     * Loads likes created by a user, fetching each related moodboard.
     *
     * @param likerUsername username of the liker
     * @return likes by that user with moodboard association initialized
     */
    @EntityGraph(attributePaths = "moodboard")

    List<MoodboardLike> findByLiker_Username(String likerUsername);



    /**
     * @param moodboardId moodboard primary key
     * @return number of likes on the moodboard
     */
    long countByMoodboard_Id(Long moodboardId);

    /**
     * Removes all likes created by a user (e.g. on account deletion).
     *
     * @param likerUsername username of the liker
     */
    @Transactional
    void deleteByLiker_Username(String likerUsername);

}

