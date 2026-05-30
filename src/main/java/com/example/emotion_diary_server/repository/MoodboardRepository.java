package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.Moodboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodboardRepository extends JpaRepository<Moodboard, Long> {
    List<Moodboard> findByOwnerUsername(String ownerUsername);

    Page<Moodboard> findByIsPublicTrueAndOwnerUsernameNotOrderByIdDesc(
            String ownerUsername, Pageable pageable);
}
