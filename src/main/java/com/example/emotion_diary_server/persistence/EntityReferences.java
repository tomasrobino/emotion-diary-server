package com.example.emotion_diary_server.persistence;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class EntityReferences {

    private final UserRepository userRepository;
    private final MoodboardRepository moodboardRepository;

    public EntityReferences(UserRepository userRepository, MoodboardRepository moodboardRepository) {
        this.userRepository = userRepository;
        this.moodboardRepository = moodboardRepository;
    }

    public User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public Moodboard requireMoodboard(Long moodboardId) {
        return moodboardRepository.findById(moodboardId)
                .orElseThrow(() -> new IllegalArgumentException("Moodboard not found: " + moodboardId));
    }
}
