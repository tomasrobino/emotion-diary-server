package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.LikedMoodboardSummaryDto;
import com.example.emotion_diary_server.dto.MediaUploadResponseDto;
import com.example.emotion_diary_server.dto.MoodboardContentDto;
import com.example.emotion_diary_server.dto.MoodboardCreateRequestDto;
import com.example.emotion_diary_server.dto.MoodboardRenameRequestDto;
import com.example.emotion_diary_server.dto.MoodboardResponseDto;
import com.example.emotion_diary_server.dto.MoodboardsPageDto;
import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.model.MoodboardMedia;
import com.example.emotion_diary_server.security.MoodboardAccessService;
import com.example.emotion_diary_server.security.MoodboardPermissionService;
import com.example.emotion_diary_server.service.MoodboardContentService;
import com.example.emotion_diary_server.service.MoodboardLikeService;
import com.example.emotion_diary_server.service.MoodboardMediaService;
import com.example.emotion_diary_server.service.MoodboardNameService;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.service.MoodboardService;
import com.example.emotion_diary_server.user.User;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
public class MoodboardController {

    private static final int DEFAULT_PAGE_SIZE = 24;
    private static final int MAX_PAGE_SIZE = 50;

    private final MoodboardPermissionService permissionService;
    private final MoodboardLikeService likeService;
    private final MoodboardService moodboardService;
    private final MoodboardAccessService moodboardAccessService;
    private final MoodboardContentService contentService;
    private final MoodboardMediaService mediaService;
    private final MoodboardNameService nameService;
    private final EntityReferences entityReferences;

    public MoodboardController(
            MoodboardPermissionService permissionService,
            MoodboardLikeService likeService,
            MoodboardService moodboardService,
            MoodboardAccessService moodboardAccessService,
            MoodboardContentService contentService,
            MoodboardMediaService mediaService,
            MoodboardNameService nameService,
            EntityReferences entityReferences
    ) {
        this.permissionService = permissionService;
        this.likeService = likeService;
        this.moodboardService = moodboardService;
        this.moodboardAccessService = moodboardAccessService;
        this.contentService = contentService;
        this.mediaService = mediaService;
        this.nameService = nameService;
        this.entityReferences = entityReferences;
    }

    /**
     * GET /{user}/moodboards?page=0&size=24
     * Returns a paginated list of moodboards the requester can access (newest first).
     * Without page/size query params, returns the full accessible list (legacy).
     */
    @GetMapping("/{user}/moodboards")
    public ResponseEntity<?> getMoodboards(
            @PathVariable String user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        String principalName = authentication.getName();
        if (page == null && size == null) {
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

        int pageIndex = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size >= 1 ? size : DEFAULT_PAGE_SIZE;
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        Page<Moodboard> result = moodboardService.findByOwnerUsername(user, PageRequest.of(pageIndex, pageSize));
        List<MoodboardResponseDto> items = result.getContent()
                .stream()
                .filter(m -> {
                    Long id = m.getId();
                    return id != null && moodboardAccessService.canAccess(id, user, principalName);
                })
                .map(this::toResponseDto)
                .toList();

        return ResponseEntity.ok(new MoodboardsPageDto(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        ));
    }

    /**
     * GET /{user}/moodboards/{moodboardId}
     * Returns a single moodboard if the requester can access it.
     */
    @GetMapping("/{user}/moodboards/{moodboardId}")
    public ResponseEntity<MoodboardResponseDto> getMoodboard(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @Nullable Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, principalName(authentication))) {
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
        User owner = entityReferences.requireUser(user);
        Moodboard moodboard = new Moodboard(owner, contentService.serialize(content));
        moodboard.setName(nameService.normalizeForCreate(request.getName()));
        moodboard = moodboardService.save(moodboard);
        Long createdId = moodboard.getId();
        return ResponseEntity
                .created(URI.create("/" + user + "/moodboards/" + createdId))
                .body(toResponseDto(moodboard));
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
     * PUT /{user}/moodboards/{moodboardId}/thumbnail
     * Uploads or replaces the moodboard thumbnail JPEG. Owner only.
     */
    @PutMapping(value = "/{user}/moodboards/{moodboardId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> uploadThumbnail(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)) {
            throw new IllegalArgumentException("La miniatura debe ser JPEG");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        moodboard.setThumbnail(file.getBytes());
        moodboardService.update(moodboard);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /{user}/moodboards/{moodboardId}/thumbnail
     * Streams the moodboard thumbnail. Requester must have access to the moodboard.
     */
    @GetMapping("/{user}/moodboards/{moodboardId}/thumbnail")
    public ResponseEntity<byte[]> getThumbnail(
            @PathVariable String user,
            @PathVariable Long moodboardId,
            @Nullable Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, principalName(authentication))) {
            return ResponseEntity.notFound().build();
        }
        byte[] thumbnail = moodboard.getThumbnail();
        if (thumbnail == null || thumbnail.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(thumbnail);
    }

    /**
     * POST /{user}/moodboards/{moodboardId}/media
     * Uploads an image asset for the moodboard. Owner only.
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
        MoodboardMedia media = mediaService.upload(moodboard, file);
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
            @Nullable Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, principalName(authentication))) {
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
        permissionService.grantAccess(moodboard, user, grantTo);
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
        permissionService.revokeAccess(moodboardId, revokeFrom);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /{user}/moodboards/{moodboardId}/permissions
     * Lists usernames with explicit access. Owner only.
     */
    @GetMapping("/{user}/moodboards/{moodboardId}/permissions")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<List<String>> listPermissions(
            @PathVariable String user,
            @PathVariable Long moodboardId
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        List<String> granted = permissionService.listPermittedUsernames(moodboardId);
        return ResponseEntity.ok(granted);
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
        likeService.like(moodboard, principalName);
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
        likeService.unlike(moodboardId, authentication.getName());
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
            @Nullable Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, principalName(authentication))) {
            return ResponseEntity.notFound().build();
        }
        List<String> likers = likeService.getLikerUsernames(moodboardId);
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
            @Nullable Authentication authentication
    ) {
        Moodboard moodboard = moodboardService.findById(moodboardId);
        if (moodboard == null || !user.equals(moodboard.getOwnerUsername())) {
            return ResponseEntity.notFound().build();
        }
        if (!moodboardAccessService.canAccess(moodboardId, user, principalName(authentication))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(likeService.countByMoodboardId(moodboardId));
    }

    /**
     * GET /{user}/liked-moodboards
     * Returns summaries of moodboards the user has liked.
     */
    @GetMapping("/{user}/liked-moodboards")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<List<LikedMoodboardSummaryDto>> getLikedMoodboards(@PathVariable String user) {
        List<LikedMoodboardSummaryDto> summaries = likeService.getLikedMoodboards(user)
                .stream()
                .map(moodboard -> {
                    Long id = moodboard.getId();
                    long likeCount = id != null ? likeService.countByMoodboardId(id) : 0L;
                    return LikedMoodboardSummaryDto.from(moodboard, likeCount);
                })
                .toList();
        return ResponseEntity.ok(summaries);
    }

    private MoodboardResponseDto toResponseDto(Moodboard moodboard) {
        MoodboardContentDto content = contentService.deserialize(moodboard.getContent());
        Long id = moodboard.getId();
        long likeCount = id != null ? likeService.countByMoodboardId(id) : 0L;
        return MoodboardResponseDto.from(moodboard, content, likeCount);
    }

    private static String principalName(@Nullable Authentication authentication) {
        return authentication != null ? authentication.getName() : "";
    }

    private static String contentDisposition(MoodboardMedia media) {
        if (media.getOriginalFilename() != null && !media.getOriginalFilename().isBlank()) {
            return "inline; filename=\"" + media.getOriginalFilename().replace("\"", "") + "\"";
        }
        return "inline";
    }
}
