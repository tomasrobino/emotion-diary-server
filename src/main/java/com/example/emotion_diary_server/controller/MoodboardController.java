package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.security.MoodboardPermission;
import com.example.emotion_diary_server.security.MoodboardPermissionRepository;
import com.example.emotion_diary_server.service.MoodboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MoodboardController {

    private final MoodboardPermissionRepository permissionRepository;
    private final MoodboardService moodboardService;

    public MoodboardController(MoodboardPermissionRepository permissionRepository, MoodboardService moodboardService) {
        this.permissionRepository = permissionRepository;
        this.moodboardService = moodboardService;
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
        List<String> moodboards = moodboardService.findByOwnerUsername(user)
                .stream()
                .map(Moodboard::getContent)
                .toList();
        return ResponseEntity.ok(moodboards);
    }

    /**
     * POST /{user}/moodboards
     * Only the owner can create their own moodboards.
     */
    @PostMapping("/{user}/moodboards")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Moodboard> createMoodboard(
            @PathVariable String user,
            @RequestBody String content
    ) {
        Moodboard moodboard = moodboardService.save(new Moodboard(user, content));
        return ResponseEntity.ok(moodboard);
    }

    /**
     * PUT /{user}/moodboards/{moodboardId}
     * Only the owner can update their own moodboards.
     */
    @PutMapping("/{user}/moodboards/{moodboardId}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Moodboard> updateMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestBody String content
    ) {
        Moodboard existing = moodboardService.findById(moodboardId);
        if (existing == null || !existing.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        Moodboard moodboard = moodboardService.update(new Moodboard(moodboardId, user, content));
        return ResponseEntity.ok(moodboard);
    }

    /**
     * DELETE /{user}/moodboards/{moodboardId}
     * Only the owner can delete their own moodboards.
     */
    @DeleteMapping("/{user}/moodboards/{moodboardId}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> deleteMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId
    ) {
        Moodboard existing = moodboardService.findById(moodboardId);
        if (existing == null || !existing.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        moodboardService.deleteById(moodboardId);
        return ResponseEntity.ok().build();
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