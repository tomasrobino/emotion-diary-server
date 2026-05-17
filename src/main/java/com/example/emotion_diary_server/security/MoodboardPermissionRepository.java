package com.example.emotion_diary_server.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodboardPermissionRepository extends JpaRepository<MoodboardPermission, Long> {

    boolean existsByOwnerUsernameAndPermittedUsername(String ownerUsername, String permittedUsername);

    void deleteByOwnerUsernameAndPermittedUsername(String ownerUsername, String permittedUsername);
}