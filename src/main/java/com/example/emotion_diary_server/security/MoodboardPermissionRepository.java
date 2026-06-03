package com.example.emotion_diary_server.security;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence access for {@link MoodboardPermission} grants.
 */
public interface MoodboardPermissionRepository extends JpaRepository<MoodboardPermission, Long> {

    /**
     * @param moodboardId        moodboard identifier
     * @param permittedUsername  username to check
     * @return {@code true} if an explicit grant exists for that user and moodboard
     */
    boolean existsByMoodboard_IdAndPermitted_Username(Long moodboardId, String permittedUsername);

    /**
     * Removes the grant for a specific user on a moodboard.
     *
     * @param moodboardId        moodboard identifier
     * @param permittedUsername  username whose grant is removed
     */
    @Transactional
    void deleteByMoodboard_IdAndPermitted_Username(Long moodboardId, String permittedUsername);

    /**
     * Removes all grants associated with a moodboard.
     *
     * @param moodboardId moodboard identifier
     */
    @Transactional
    void deleteByMoodboard_Id(Long moodboardId);

    /**
     * @param moodboardId moodboard identifier
     * @return all permission rows for the moodboard
     */
    List<MoodboardPermission> findByMoodboard_Id(Long moodboardId);

    /**
     * Removes every grant where the given user was the permitted party.
     *
     * @param permittedUsername username whose grants are removed
     */
    @Transactional
    void deleteByPermitted_Username(String permittedUsername);
}
