package com.example.emotion_diary_server.security;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodboardPermissionRepository extends JpaRepository<MoodboardPermission, Long> {

    boolean existsByOwnerUsernameAndPermittedUsername(String ownerUsername, String permittedUsername);
    @Transactional
    void deleteByOwnerUsernameAndPermittedUsername(String ownerUsername, String permittedUsername);
}