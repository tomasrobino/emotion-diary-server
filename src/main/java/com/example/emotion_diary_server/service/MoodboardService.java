package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardLikeRepository;
import com.example.emotion_diary_server.repository.MoodboardMediaRepository;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import com.example.emotion_diary_server.security.MoodboardPermissionRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodboardService {
    private final MoodboardRepository moodboardRepository;
    private final MoodboardPermissionRepository permissionRepository;
    private final MoodboardLikeRepository likeRepository;
    private final MoodboardMediaRepository mediaRepository;

    public MoodboardService(
            MoodboardRepository moodboardRepository,
            MoodboardPermissionRepository permissionRepository,
            MoodboardLikeRepository likeRepository,
            MoodboardMediaRepository mediaRepository
    ) {
        this.moodboardRepository = moodboardRepository;
        this.permissionRepository = permissionRepository;
        this.likeRepository = likeRepository;
        this.mediaRepository = mediaRepository;
    }

    public Moodboard save(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }

    public @Nullable Moodboard findById(Long id) {
        return moodboardRepository.findById(id).orElse(null);
    }

    public Iterable<Moodboard> findAll() {
        return moodboardRepository.findAll();
    }

    public void deleteById(Long id) {
        permissionRepository.deleteByMoodboardId(id);
        likeRepository.deleteByMoodboardId(id);
        mediaRepository.deleteByMoodboardId(id);
        moodboardRepository.deleteById(id);
    }

    public void deleteAll() {
        moodboardRepository.deleteAll();
    }

    public List<Moodboard> findByOwnerUsername(String ownerUsername) {
        return moodboardRepository.findByOwnerUsername(ownerUsername);
    }

    public Page<Moodboard> findByOwnerUsername(String ownerUsername, Pageable pageable) {
        return moodboardRepository.findByOwnerUsernameOrderByIdDesc(ownerUsername, pageable);
    }

    public Page<Moodboard> findPublicByOtherUsers(String excludeOwnerUsername, Pageable pageable) {
        return moodboardRepository.findByIsPublicTrueAndOwnerUsernameNotOrderByIdDesc(
                excludeOwnerUsername, pageable);
    }

    public Moodboard update(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }
}
