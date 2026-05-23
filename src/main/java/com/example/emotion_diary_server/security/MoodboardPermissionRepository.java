package com.example.emotion_diary_server.security;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodboardPermissionRepository extends JpaRepository<MoodboardPermission, Long> {

    boolean existsByMoodboardIdAndPermittedUsername(Long moodboardId, String permittedUsername);

    @Transactional
    void deleteByMoodboardIdAndPermittedUsername(Long moodboardId, String permittedUsername);

    @Transactional
    void deleteByMoodboardId(Long moodboardId);
}