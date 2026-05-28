package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.MoodboardMedia;
import com.example.emotion_diary_server.repository.MoodboardMediaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MoodboardMediaService {

    private final MoodboardMediaRepository mediaRepository;

    public MoodboardMediaService(MoodboardMediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public MoodboardMedia upload(Long moodboardId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        byte[] data = file.getBytes();
        MoodboardMedia media = new MoodboardMedia(
                moodboardId,
                contentType,
                file.getOriginalFilename(),
                data
        );
        return mediaRepository.save(media);
    }

    public @Nullable MoodboardMedia findByIdAndMoodboardId(Long assetId, Long moodboardId) {
        return mediaRepository.findByIdAndMoodboardId(assetId, moodboardId).orElse(null);
    }

    public void deleteByIdAndMoodboardId(Long assetId, Long moodboardId) {
        mediaRepository.findByIdAndMoodboardId(assetId, moodboardId)
                .ifPresent(mediaRepository::delete);
    }

    public void deleteByMoodboardId(Long moodboardId) {
        mediaRepository.deleteByMoodboardId(moodboardId);
    }
}
