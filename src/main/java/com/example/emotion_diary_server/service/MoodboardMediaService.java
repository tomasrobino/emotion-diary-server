package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.model.MoodboardMedia;
import com.example.emotion_diary_server.repository.MoodboardMediaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

/**
 * Stores and retrieves image assets attached to a moodboard.
 */
@Service
public class MoodboardMediaService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final MoodboardMediaRepository mediaRepository;

    /**
     * @param mediaRepository repository for moodboard media rows
     */
    public MoodboardMediaService(MoodboardMediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    /**
     * Uploads an image file and associates it with the given moodboard.
     *
     * @param moodboard parent moodboard
     * @param file      multipart upload
     * @return persisted media entity
     * @throws IllegalArgumentException    if the file is empty
     * @throws ResponseStatusException     with {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE} if type is missing or not allowed
     * @throws IOException                 if reading file bytes fails
     */
    public MoodboardMedia upload(Moodboard moodboard, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type is required");
        }
        String normalizedType = contentType.toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedType)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only JPEG, PNG, WebP, and GIF images are allowed"
            );
        }
        byte[] data = file.getBytes();
        MoodboardMedia media = new MoodboardMedia(
                moodboard,
                normalizedType,
                file.getOriginalFilename(),
                data
        );
        return mediaRepository.save(media);
    }

    /**
     * Loads media scoped to a specific moodboard.
     *
     * @param assetId     media primary key
     * @param moodboardId parent moodboard id
     * @return the media row, or {@code null} if not found or not linked to that moodboard
     */
    public @Nullable MoodboardMedia findByIdAndMoodboardId(Long assetId, Long moodboardId) {
        return mediaRepository.findByIdAndMoodboard_Id(assetId, moodboardId).orElse(null);
    }

    /**
     * Deletes a media row when it belongs to the given moodboard.
     *
     * @param assetId     media primary key
     * @param moodboardId parent moodboard id
     */
    public void deleteByIdAndMoodboardId(Long assetId, Long moodboardId) {
        mediaRepository.findByIdAndMoodboard_Id(assetId, moodboardId)
                .ifPresent(mediaRepository::delete);
    }

    /**
     * Removes all media assets for a moodboard.
     *
     * @param moodboardId parent moodboard id
     */
    public void deleteByMoodboardId(Long moodboardId) {
        mediaRepository.deleteByMoodboard_Id(moodboardId);
    }
}
