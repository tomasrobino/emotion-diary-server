package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.LikedMoodboardSummaryDto;
import com.example.emotion_diary_server.dto.MediaUploadResponseDto;
import com.example.emotion_diary_server.dto.MoodboardContentDto;
import com.example.emotion_diary_server.dto.MoodboardCreateRequestDto;
import com.example.emotion_diary_server.dto.MoodboardRenameRequestDto;
import com.example.emotion_diary_server.dto.MoodboardResponseDto;
import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.model.MoodboardLike;
import com.example.emotion_diary_server.model.MoodboardMedia;
import com.example.emotion_diary_server.repository.MoodboardLikeRepository;
import com.example.emotion_diary_server.security.MoodboardAccessService;
import com.example.emotion_diary_server.security.MoodboardPermission;
import com.example.emotion_diary_server.security.MoodboardPermissionRepository;
import com.example.emotion_diary_server.service.MoodboardContentService;
import com.example.emotion_diary_server.service.MoodboardMediaService;
import com.example.emotion_diary_server.service.MoodboardNameService;
import com.example.emotion_diary_server.service.MoodboardService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
public class MoodboardController {

    private final MoodboardPermissionRepository permissionRepository;
    private final MoodboardLikeRepository likeRepository;
    private final MoodboardService moodboardService;
    private final MoodboardAccessService moodboardAccessService;
    private final MoodboardContentService contentService;
    private final MoodboardMediaService mediaService;
    private final MoodboardNameService nameService;

    public MoodboardController(
            MoodboardPermissionRepository permissionRepository,
            MoodboardLikeRepository likeRepository,
            MoodboardService moodboardService,
            MoodboardAccessService moodboardAccessService,
            MoodboardContentService contentService,
            MoodboardMediaService mediaService,
            MoodboardNameService nameService
    ) {
        this.permissionRepository = permissionRepository;
        this.likeRepository = likeRepository;
        this.moodboardService = moodboardService;
        this.moodboardAccessService = moodboardAccessService;
        this.contentService = contentService;
        this.mediaService = mediaService;
        this.nameService = nameService;
    }

    /**
     * GET /{user}/moodboards
     * Returns only the moodboards the requester can access:
     *  - all moodboards, if the requester is the owner
     *  - public moodboards and those with explicit per-moodboard permission, otherwise
     */
    @GetMapping("/{user}/moodboards")
    public ResponseEntity<List<MoodboardResponseDto>> getMoodboards(
            @PathVariable String user,
            Authentication authentication
    ) {
        String principalName = authentication.getName();
        List<MoodboardResponseDto> moodboards = moodboardService.findByOwnerUsername(user)
                .stream()
                .filter(m -> {
                    Long id = m.getId();
                    return id != null && moodboardAccessService.canAccess(id, user, principalName);
                })
                .map(this::toResponseDto)
                .toList();
        return ResponseEntity.ok(moodboards);
    }

    /**
     * GET /{user}/moodboards/{moodboardId}
     * Returns a single moodboard if the requester can access it.
     */
    @GetMapping("/{user}/moodboards/{moodboardId}")
    public ResponseEntity<MoodboardResponseDto> getMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponseDto(moodboard));
    }

    /**
     * POST /{user}/moodboards
     * Only the owner can create their own moodboards.
     */
    @PostMapping("/{user}/moodboards")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<MoodboardResponseDto> createMoodboard(
            @PathVariable String user,
            @RequestBody @Nullable MoodboardCreateRequestDto request
    ) {
        if (request == null || request.getContent() == null) {
            throw new IllegalArgumentException("Se requiere el contenido del moodboard");
        }
        MoodboardContentDto content = request.getContent();
        contentService.validateForCreate(content);
        Moodboard moodboard = new Moodboard(user, contentService.serialize(content));
        moodboard.setName(nameService.normalizeForCreate(request.getName()));
        moodboard = moodboardService.save(moodboard);
        return ResponseEntity.ok(toResponseDto(moodboard));
    }

    /**
     * PATCH /{user}/moodboards/{moodboardId}
     * Only the owner can rename their own moodboards.
     */
    @PatchMapping("/{user}/moodboards/{moodboardId}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<MoodboardResponseDto> renameMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestBody @Nullable MoodboardRenameRequestDto request
    ) {
        Moodboard existing = moodboardService.findById(moodboardId);
        if (existing == null || !user.equals(existing.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (request == null) {
            throw new IllegalArgumentException("Se requiere el nombre del moodboard");
        }
        existing.setName(nameService.validateForRename(request.getName()));
        Moodboard moodboard = moodboardService.update(existing);
        return ResponseEntity.ok(toResponseDto(moodboard));
    }

    /**
     * PUT /{user}/moodboards/{moodboardId}
     * Only the owner can update their own moodboards.
     */
    @PutMapping("/{user}/moodboards/{moodboardId}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<MoodboardResponseDto> updateMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestBody @Nullable MoodboardContentDto content
    ) {
        Moodboard existing = moodboardService.findById(moodboardId);
        if (existing == null || !user.equals(existing.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        contentService.validate(content, moodboardId);
        existing.setContent(contentService.serialize(Objects.requireNonNull(content)));
        Moodboard moodboard = moodboardService.update(existing);
        return ResponseEntity.ok(toResponseDto(moodboard));
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
        if (existing == null || !user.equals(existing.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        moodboardService.deleteById(moodboardId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /{user}/moodboards/{moodboardId}/media
     * Uploads an image or video asset for the moodboard. Owner only.
     */
    @PostMapping(value = "/{user}/moodboards/{moodboardId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<MediaUploadResponseDto> uploadMedia(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        MoodboardMedia media = mediaService.upload(moodboardId, file);
        return ResponseEntity.ok(new MediaUploadResponseDto(
                Objects.requireNonNull(media.getId()),
                media.getContentType(),
                media.getSizeBytes()
        ));
    }

    /**
     * GET /{user}/moodboards/{moodboardId}/media/{assetId}
     * Streams a media asset. Requester must have access to the moodboard.
     */
    @GetMapping("/{user}/moodboards/{moodboardId}/media/{assetId}")
    public ResponseEntity<byte[]> getMedia(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @PathVariable Long assetId,
            Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        MoodboardMedia media = mediaService.findByIdAndMoodboardId(assetId, moodboardId);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(media))
                .body(media.getData());
    }

    /**
     * DELETE /{user}/moodboards/{moodboardId}/media/{assetId}
     * Deletes a media asset. Owner only.
     */
    @DeleteMapping("/{user}/moodboards/{moodboardId}/media/{assetId}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @PathVariable Long assetId
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        MoodboardMedia media = mediaService.findByIdAndMoodboardId(assetId, moodboardId);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }
        mediaService.deleteByIdAndMoodboardId(assetId, moodboardId);
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
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!permissionRepository.existsByMoodboardIdAndPermittedUsername(moodboardId, grantTo)) {
            permissionRepository.save(new MoodboardPermission(moodboardId, user, grantTo));
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
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
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
    public ResponseEntity<MoodboardResponseDto> setVisibility(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestParam boolean isPublic
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        moodboard.setPublic(isPublic);
        return ResponseEntity.ok(toResponseDto(moodboardService.update(moodboard)));
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
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
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
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
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
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
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
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(likeRepository.countByMoodboardId(moodboardId));
    }

    /**
     * GET /{user}/liked-moodboards
     * Returns summaries of moodboards the user has liked.
     */
    @GetMapping("/{user}/liked-moodboards")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<List<LikedMoodboardSummaryDto>> getLikedMoodboards(@PathVariable String user) {
        List<LikedMoodboardSummaryDto> summaries = likeRepository.findByLikerUsername(user)
                .stream()
                .map(MoodboardLike::getMoodboardId)
                .map(moodboardService::findById)
                .filter(Objects::nonNull)
                .map(LikedMoodboardSummaryDto::from)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    private MoodboardResponseDto toResponseDto(Moodboard moodboard) {
        MoodboardContentDto content = contentService.deserialize(moodboard.getContent());
        return MoodboardResponseDto.from(moodboard, content);
    }

    private static String contentDisposition(MoodboardMedia media) {
        if (media.getOriginalFilename() != null && !media.getOriginalFilename().isBlank()) {
            return "inline; filename=\"" + media.getOriginalFilename().replace("\"", "") + "\"";
        }
        return "inline";
    }
}
