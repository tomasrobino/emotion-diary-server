package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Persistence operations for {@link Moodboard} entities, including owner-scoped and public listings.
 */
@Service
public class MoodboardService {
    private final MoodboardRepository moodboardRepository;

    /**
     * @param moodboardRepository repository for moodboard entities
     */
    public MoodboardService(MoodboardRepository moodboardRepository) {
        this.moodboardRepository = moodboardRepository;
    }

    /**
     * Persists a moodboard (insert or update).
     *
     * @param moodboard entity to save
     * @return the saved moodboard
     */
    public Moodboard save(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }

    /**
     * Loads a moodboard by primary key.
     *
     * @param id moodboard id
     * @return the moodboard, or {@code null} if not found
     */
    public @Nullable Moodboard findById(Long id) {
        return moodboardRepository.findById(id).orElse(null);
    }

    /**
     * Returns every moodboard in the database.
     *
     * @return all moodboards
     */
    public Iterable<Moodboard> findAll() {
        return moodboardRepository.findAll();
    }

    /**
     * Deletes a moodboard by id.
     *
     * @param id moodboard id
     */
    public void deleteById(Long id) {
        moodboardRepository.deleteById(id);
    }

    /**
     * Removes all moodboards from the database.
     */
    public void deleteAll() {
        moodboardRepository.deleteAll();
    }

    /**
     * Lists moodboards owned by the given user (unpaged).
     *
     * @param ownerUsername owner username
     * @return moodboards for that owner
     */
    public List<Moodboard> findByOwnerUsername(String ownerUsername) {
        return moodboardRepository.findByOwner_Username(ownerUsername);
    }

    /**
     * Lists moodboards owned by the given user, newest first.
     *
     * @param ownerUsername owner username
     * @param pageable      pagination and sort
     * @return a page of moodboards
     */
    public Page<Moodboard> findByOwnerUsername(String ownerUsername, Pageable pageable) {
        return moodboardRepository.findByOwner_UsernameOrderByIdDesc(ownerUsername, pageable);
    }

    /**
     * Lists public moodboards from users other than the excluded owner, newest first.
     *
     * @param excludeOwnerUsername username to exclude from results
     * @param pageable             pagination and sort
     * @return a page of public moodboards
     */
    public Page<Moodboard> findPublicByOtherUsers(String excludeOwnerUsername, Pageable pageable) {
        return moodboardRepository.findByIsPublicTrueAndOwner_UsernameNotOrderByIdDesc(
                excludeOwnerUsername, pageable);
    }

    /**
     * Updates an existing moodboard by saving the given entity.
     *
     * @param moodboard entity with updated fields
     * @return the saved moodboard
     */
    public Moodboard update(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }
}
