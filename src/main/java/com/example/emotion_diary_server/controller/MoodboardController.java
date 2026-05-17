package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.security.MoodboardAccessService;
import com.example.emotion_diary_server.security.MoodboardPermission;
import com.example.emotion_diary_server.security.MoodboardPermissionRepository;
import com.example.emotion_diary_server.service.MoodboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MoodboardController {

    private final MoodboardPermissionRepository permissionRepository;
    private final MoodboardService moodboardService;
    private final MoodboardAccessService moodboardAccessService;

    public MoodboardController(MoodboardPermissionRepository permissionRepository, MoodboardService moodboardService, MoodboardAccessService moodboardAccessService) {
        this.permissionRepository = permissionRepository;
        this.moodboardService = moodboardService;
        this.moodboardAccessService = moodboardAccessService;
    }

    /**
     * GET /{user}/moodboards
     * Returns only the moodboards the requester can access:
     *  - all moodboards, if the requester is the owner
     *  - only those with explicit per-moodboard permission, otherwise
     */
    @GetMapping("/{user}/moodboards")
    public ResponseEntity<List<String>> getMoodboards(
            @PathVariable String user,
            Authentication authentication
    ) {
        String principalName = authentication.getName();
        List<String> moodboards = moodboardService.findByOwnerUsername(user)
                .stream()
                .filter(m -> moodboardAccessService.canAccess(m.getId(), user, principalName))
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
     * POST /{user}/moodboards/{moodboardId}/permissions?grantTo={otherUser}
     * Only the owner can grant access to a specific moodboard.
     */
    @PostMapping("/{user}/moodboards/{moodboardId}/permissions")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> grantAccess(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestParam String grantTo
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        if (!permissionRepository.existsByMoodboardIdAndPermittedUsername(moodboardId, grantTo)) {
            permissionRepository.save(new MoodboardPermission(moodboardId, grantTo));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /{user}/moodboards/{moodboardId}/permissions?revokeFrom={otherUser}
     * Only the owner can revoke access to a specific moodboard.
     */
    @DeleteMapping("/{user}/moodboards/{moodboardId}/permissions")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> revokeAccess(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestParam String revokeFrom
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        permissionRepository.deleteByMoodboardIdAndPermittedUsername(moodboardId, revokeFrom);
        return ResponseEntity.ok().build();
    }
}