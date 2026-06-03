package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.Moodboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence access for {@link Moodboard} rows.
 */
public interface MoodboardRepository extends JpaRepository<Moodboard, Long> {

    /**
     * @param ownerUsername moodboard owner username
     * @return all moodboards owned by the user
     */
    List<Moodboard> findByOwner_Username(String ownerUsername);

    /**
     * @param ownerUsername moodboard owner username
     * @param pageable      paging and sort (newest id first in the query name)
     * @return a page of moodboards owned by the user
     */
    Page<Moodboard> findByOwner_UsernameOrderByIdDesc(String ownerUsername, Pageable pageable);

    /**
     * Public moodboards from other users, newest first.
     *
     * @param ownerUsername username to exclude (typically the current user)
     * @param pageable      paging and sort
     * @return a page of public moodboards not owned by {@code ownerUsername}
     */
    Page<Moodboard> findByIsPublicTrueAndOwner_UsernameNotOrderByIdDesc(
            String ownerUsername, Pageable pageable);
}
