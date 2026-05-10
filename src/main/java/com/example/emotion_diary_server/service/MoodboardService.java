package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import org.springframework.stereotype.Service;

@Service
public class MoodboardService {
    private final MoodboardRepository moodboardRepository;

    public MoodboardService(MoodboardRepository moodboardRepository) {
        this.moodboardRepository = moodboardRepository;
    }

    public void save(Moodboard moodboard) {
        moodboardRepository.save(moodboard);
    }

    public Moodboard findById(Long id) {
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

    public void update(Moodboard moodboard) {
        moodboardRepository.save(moodboard);
    }
}
