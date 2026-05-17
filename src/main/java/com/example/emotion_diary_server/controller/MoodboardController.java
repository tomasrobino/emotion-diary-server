package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.security.MoodboardPermission;
import com.example.emotion_diary_server.security.MoodboardPermissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MoodboardController {

    private final MoodboardPermissionRepository permissionRepository;

    public MoodboardController(MoodboardPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * GET /{user}/moodboards
     * Accessible only by:
     *  - the owner ({user} themselves), or
     *  - another user who has been explicitly granted access
     */
    @GetMapping("/{user}/moodboards")
    @PreAuthorize("@moodboardAccess.canAccess(#user, authentication.name)")
    public ResponseEntity<List<String>> getMoodboards(@PathVariable String user) {
        // Replace with real moodboard fetching logic
        return ResponseEntity.ok(List.of("moodboard1", "moodboard2"));
    }

    /**
     * POST /{user}/moodboards/permissions?grantTo={otherUser}
     * Only the owner can grant access to their own moodboards.
     */
    @PostMapping("/{user}/moodboards/permissions")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> grantAccess(
            @PathVariable String user,
            @RequestParam String grantTo
    ) {
        if (!permissionRepository.existsByOwnerUsernameAndPermittedUsername(user, grantTo)) {
            permissionRepository.save(new MoodboardPermission(user, grantTo));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /{user}/moodboards/permissions?revokeFrom={otherUser}
     * Only the owner can revoke access.
     */
    @DeleteMapping("/{user}/moodboards/permissions")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> revokeAccess(
            @PathVariable String user,
            @RequestParam String revokeFrom
    ) {
        permissionRepository.deleteByOwnerUsernameAndPermittedUsername(user, revokeFrom);
        return ResponseEntity.ok().build();
    }
}