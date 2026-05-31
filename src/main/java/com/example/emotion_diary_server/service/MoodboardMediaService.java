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

@Service
public class MoodboardMediaService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final MoodboardMediaRepository mediaRepository;

    public MoodboardMediaService(MoodboardMediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

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

    public @Nullable MoodboardMedia findByIdAndMoodboardId(Long assetId, Long moodboardId) {
        return mediaRepository.findByIdAndMoodboard_Id(assetId, moodboardId).orElse(null);
    }

    public void deleteByIdAndMoodboardId(Long assetId, Long moodboardId) {
        mediaRepository.findByIdAndMoodboard_Id(assetId, moodboardId)
                .ifPresent(mediaRepository::delete);
    }

    public void deleteByMoodboardId(Long moodboardId) {
        mediaRepository.deleteByMoodboard_Id(moodboardId);
    }
}
