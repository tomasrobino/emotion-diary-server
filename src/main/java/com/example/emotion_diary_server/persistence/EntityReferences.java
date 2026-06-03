package com.example.emotion_diary_server.persistence;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Resolves persisted entities by identifier and fails fast when they are missing.
 */
@Component
public class EntityReferences {

    private final UserRepository userRepository;
    private final MoodboardRepository moodboardRepository;

    /**
     * @param userRepository      repository used to load users
     * @param moodboardRepository repository used to load moodboards
     */
    public EntityReferences(UserRepository userRepository, MoodboardRepository moodboardRepository) {
        this.userRepository = userRepository;
        this.moodboardRepository = moodboardRepository;
    }

    /**
     * Loads a user by username.
     *
     * @param username unique username
     * @return the persisted user
     * @throws IllegalArgumentException if no user exists for the username
     */
    public User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    /**
     * Loads a moodboard by id.
     *
     * @param moodboardId primary key of the moodboard
     * @return the persisted moodboard
     * @throws IllegalArgumentException if no moodboard exists for the id
     */
    public Moodboard requireMoodboard(Long moodboardId) {
        return moodboardRepository.findById(moodboardId)
                .orElseThrow(() -> new IllegalArgumentException("Moodboard not found: " + moodboardId));
    }
}
