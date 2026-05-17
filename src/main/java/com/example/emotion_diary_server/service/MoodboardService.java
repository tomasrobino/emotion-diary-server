package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodboardService {
    private final MoodboardRepository moodboardRepository;

    public MoodboardService(MoodboardRepository moodboardRepository) {
        this.moodboardRepository = moodboardRepository;
    }

    public Moodboard save(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }

    public @Nullable Moodboard findById(Long id) {
        return moodboardRepository.findById(id).orElse(null);
    }

    public Iterable<Moodboard> findAll() {
        return moodboardRepository.findAll();
    }

    public void deleteById(Long id) {
        moodboardRepository.deleteById(id);
    }

    public void deleteAll() {
        moodboardRepository.deleteAll();
    }

    public List<Moodboard> findByOwnerUsername(String ownerUsername) {
        return moodboardRepository.findByOwnerUsername(ownerUsername);
    }

    public Moodboard update(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }
}
