package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.model.MoodboardLike;
import com.example.emotion_diary_server.repository.MoodboardLikeRepository;
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
    private final MoodboardLikeRepository likeRepository;
    private final MoodboardService moodboardService;
    private final MoodboardAccessService moodboardAccessService;

    public MoodboardController(
            MoodboardPermissionRepository permissionRepository,
            MoodboardLikeRepository likeRepository,
            MoodboardService moodboardService,
            MoodboardAccessService moodboardAccessService
    ) {
        this.permissionRepository = permissionRepository;
        this.likeRepository = likeRepository;
        this.moodboardService = moodboardService;
        this.moodboardAccessService = moodboardAccessService;
    }

    /**
     * GET /{user}/moodboards
     * Returns only the moodboards the requester can access:
     *  - all moodboards, if the requester is the owner
     *  - public moodboards and those with explicit per-moodboard permission, otherwise
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
        existing.setContent(content);
        Moodboard moodboard = moodboardService.update(existing);
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

    /**
     * PUT /{user}/moodboards/{moodboardId}/visibility?isPublic=true|false
     * Only the owner can make a moodboard public or private.
     */
    @PutMapping("/{user}/moodboards/{moodboardId}/visibility")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Moodboard> setVisibility(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestParam boolean isPublic
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        moodboard.setPublic(isPublic);
        return ResponseEntity.ok(moodboardService.update(moodboard));
    }

    /**
     * POST /{user}/moodboards/{moodboardId}/likes
     * Likes a moodboard the requester can access.
     */
    @PostMapping("/{user}/moodboards/{moodboardId}/likes")
    public ResponseEntity<Void> likeMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        String principalName = authentication.getName();
        if (!moodboardAccessService.canAccess(moodboardId, user, principalName)) {
            return ResponseEntity.notFound().build();
        }
        if (!likeRepository.existsByMoodboardIdAndLikerUsername(moodboardId, principalName)) {
            likeRepository.save(new MoodboardLike(moodboardId, principalName));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /{user}/moodboards/{moodboardId}/likes
     * Removes the requester's like from a moodboard.
     */
    @DeleteMapping("/{user}/moodboards/{moodboardId}/likes")
    public ResponseEntity<Void> unlikeMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        likeRepository.deleteByMoodboardIdAndLikerUsername(moodboardId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /{user}/moodboards/{moodboardId}/likes
     * Lists usernames that liked the moodboard (requester must have access).
     */
    @GetMapping("/{user}/moodboards/{moodboardId}/likes")
    public ResponseEntity<List<String>> getLikes(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        List<String> likers = likeRepository.findByMoodboardId(moodboardId)
                .stream()
                .map(MoodboardLike::getLikerUsername)
                .toList();
        return ResponseEntity.ok(likers);
    }

    /**
     * GET /{user}/moodboards/{moodboardId}/likes/count
     * Returns how many users liked the moodboard (requester must have access).
     */
    @GetMapping("/{user}/moodboards/{moodboardId}/likes/count")
    public ResponseEntity<Long> getLikeCount(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !moodboard.getOwnerUsername().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(likeRepository.countByMoodboardId(moodboardId));
    }

    /**
     * GET /{user}/liked-moodboards
     * Returns moodboard IDs the user has liked.
     */
    @GetMapping("/{user}/liked-moodboards")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<List<Long>> getLikedMoodboards(@PathVariable String user) {
        List<Long> moodboardIds = likeRepository.findByLikerUsername(user)
                .stream()
                .map(MoodboardLike::getMoodboardId)
                .toList();
        return ResponseEntity.ok(moodboardIds);
    }
}