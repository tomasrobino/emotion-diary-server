package com.example.emotion_diary_server.security;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodboardPermissionRepository extends JpaRepository<MoodboardPermission, Long> {

    boolean existsByMoodboard_IdAndPermitted_Username(Long moodboardId, String permittedUsername);

    @Transactional
    void deleteByMoodboard_IdAndPermitted_Username(Long moodboardId, String permittedUsername);

    @Transactional
    void deleteByMoodboard_Id(Long moodboardId);

    List<MoodboardPermission> findByMoodboard_Id(Long moodboardId);
}
