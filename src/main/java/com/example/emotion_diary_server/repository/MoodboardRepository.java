package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.Moodboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodboardRepository extends JpaRepository<Moodboard, Long> {}
