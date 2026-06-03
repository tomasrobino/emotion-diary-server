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
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST endpoints for per-user moodboards: listing, CRUD, thumbnails, media assets,
 * sharing permissions, visibility, and likes. All routes are under {@code /{user}/moodboards},
 * where {@code user} is the moodboard owner's username.
 */
@Tag(name = "Moodboards")
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
     * GET /{user}/moodboards — List moodboards for a user.
     * <p>
     * Requires an authenticated principal. Results include only moodboards the requester
     * may access (owner, granted permission, or public). Optional pagination: {@code page}
     * (0-based, default 0) and {@code size} (default 24, capped at 50). When both {@code page}
     * and {@code size} are omitted, returns the full accessible list (legacy behavior).
     *
     * @param user           owner username in the path
     * @param page           optional zero-based page index
     * @param size           optional page size (1–50)
     * @param authentication current authenticated user
     * @return {@code 200 OK} with a {@link List} of {@link MoodboardResponseDto} or a {@link MoodboardsPageDto}
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
     * GET /{user}/moodboards/{moodboardId} — Get a single moodboard.
     * <p>
     * Authentication is optional; anonymous callers may read public moodboards. Returns
     * {@code 404} when the moodboard does not exist, belongs to another user, or the
     * requester lacks access.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param authentication optional current user (may be null)
     * @return {@code 200 OK} with {@link MoodboardResponseDto}, or {@code 404 Not Found}
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
     * POST /{user}/moodboards — Create a new moodboard.
     * <p>
     * Owner only ({@code #user == authentication.name}). Request body must include
     * {@link MoodboardCreateRequestDto#getContent()}; an optional display name is normalized
     * on create.
     *
     * @param user    owner username in the path (must match the authenticated user)
     * @param request create payload with content and optional name
     * @return {@code 201 Created} with {@link MoodboardResponseDto} and {@code Location} header
     * @throws IllegalArgumentException if the request or content is missing or invalid
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
     * PATCH /{user}/moodboards/{moodboardId} — Rename a moodboard.
     * <p>
     * Owner only. Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param request     rename payload with the new name
     * @return {@code 200 OK} with updated {@link MoodboardResponseDto}, or {@code 404 Not Found}
     * @throws IllegalArgumentException if the request body is null
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
     * PUT /{user}/moodboards/{moodboardId} — Replace moodboard canvas content.
     * <p>
     * Owner only. Content is validated against the moodboard and its media references.
     * Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param content     new canvas content JSON structure
     * @return {@code 200 OK} with updated {@link MoodboardResponseDto}, or {@code 404 Not Found}
     * @throws IllegalArgumentException if content is null or fails validation
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
     * DELETE /{user}/moodboards/{moodboardId} — Delete a moodboard.
     * <p>
     * Owner only. Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
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
     * PUT /{user}/moodboards/{moodboardId}/thumbnail — Upload or replace the thumbnail.
     * <p>
     * Owner only. Multipart field {@code file} must be a non-empty JPEG image.
     * Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param file        JPEG thumbnail bytes ({@code multipart/form-data})
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
     * @throws IOException              if reading the uploaded file fails
     * @throws IllegalArgumentException if the file is empty or not JPEG
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
     * GET /{user}/moodboards/{moodboardId}/thumbnail — Stream the moodboard thumbnail.
     * <p>
     * Authentication is optional; the requester must have access to the moodboard (same rules
     * as {@link #getMoodboard}). Response is cached privately for one hour.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param authentication optional current user (may be null)
     * @return {@code 200 OK} with {@code image/jpeg} body, or {@code 404 Not Found}
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
     * POST /{user}/moodboards/{moodboardId}/media — Upload an image asset for the canvas.
     * <p>
     * Owner only. Multipart field {@code file} is stored and referenced by asset id in content.
     * Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param file        image file ({@code multipart/form-data})
     * @return {@code 200 OK} with {@link MediaUploadResponseDto}, or {@code 404 Not Found}
     * @throws IOException if reading or storing the upload fails
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
     * GET /{user}/moodboards/{moodboardId}/media/{assetId} — Stream a media asset.
     * <p>
     * Authentication is optional; the requester must have access to the parent moodboard.
     * {@code Content-Disposition} is {@code inline} with the original filename when known.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param assetId        media asset identifier
     * @param authentication optional current user (may be null)
     * @return {@code 200 OK} with the asset bytes and content type, or {@code 404 Not Found}
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
     * DELETE /{user}/moodboards/{moodboardId}/media/{assetId} — Delete a media asset.
     * <p>
     * Owner only. Returns {@code 404} when the moodboard or asset does not exist for that owner.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param assetId     media asset identifier
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
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
     * POST /{user}/moodboards/{moodboardId}/permissions — Grant access to another user.
     * <p>
     * Owner only. Query parameter {@code grantTo} is the username receiving explicit access.
     * Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param grantTo     username to grant access to
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
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
     * DELETE /{user}/moodboards/{moodboardId}/permissions — Revoke access from a user.
     * <p>
     * Owner only. Query parameter {@code revokeFrom} is the username losing explicit access.
     * Returns {@code 404} when the moodboard is missing or not owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param revokeFrom  username to revoke access from
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
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
     * GET /{user}/moodboards/{moodboardId}/permissions — List users with explicit access.
     * <p>
     * Owner only. Not paginated. Returns {@code 404} when the moodboard is missing or not owned
     * by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @return {@code 200 OK} with a list of granted usernames, or {@code 404 Not Found}
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
     * PUT /{user}/moodboards/{moodboardId}/visibility — Set public or private visibility.
     * <p>
     * Owner only. Query parameter {@code isPublic} toggles whether any authenticated user may
     * discover and read the moodboard. Returns {@code 404} when the moodboard is missing or not
     * owned by {@code user}.
     *
     * @param user        owner username in the path (must match the authenticated user)
     * @param moodboardId moodboard identifier
     * @param isPublic    {@code true} for public, {@code false} for private
     * @return {@code 200 OK} with updated {@link MoodboardResponseDto}, or {@code 404 Not Found}
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
     * POST /{user}/moodboards/{moodboardId}/likes — Like a moodboard.
     * <p>
     * Requires authentication. The principal must have access to the moodboard; returns
     * {@code 404} when the moodboard is missing, mis-owned, or inaccessible. Idempotent if
     * already liked.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param authentication current authenticated user
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
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
     * DELETE /{user}/moodboards/{moodboardId}/likes — Remove the requester's like.
     * <p>
     * Requires authentication. Returns {@code 404} when the moodboard does not exist or does
     * not belong to {@code user}. Unlike is safe when no like exists.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param authentication current authenticated user
     * @return {@code 200 OK} on success, or {@code 404 Not Found}
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
     * GET /{user}/moodboards/{moodboardId}/likes — List usernames that liked the moodboard.
     * <p>
     * Authentication is optional; the requester must have access to the moodboard. Not paginated.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param authentication optional current user (may be null)
     * @return {@code 200 OK} with liker usernames, or {@code 404 Not Found}
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
     * GET /{user}/moodboards/{moodboardId}/likes/count — Get the like count.
     * <p>
     * Authentication is optional; the requester must have access to the moodboard.
     *
     * @param user           owner username in the path
     * @param moodboardId    moodboard identifier
     * @param authentication optional current user (may be null)
     * @return {@code 200 OK} with the total like count, or {@code 404 Not Found}
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
     * GET /{user}/liked-moodboards — List moodboards the user has liked.
     * <p>
     * Authenticated owner only ({@code #user == authentication.name}). Not paginated; each
     * summary includes the current like count for that moodboard.
     *
     * @param user owner username in the path (must match the authenticated user)
     * @return {@code 200 OK} with a list of {@link LikedMoodboardSummaryDto}
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
