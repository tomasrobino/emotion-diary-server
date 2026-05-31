package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.model.MoodboardLike;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.repository.MoodboardLikeRepository;
import com.example.emotion_diary_server.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodboardLikeService {

    private final MoodboardLikeRepository likeRepository;
    private final EntityReferences entityReferences;

    public MoodboardLikeService(MoodboardLikeRepository likeRepository, EntityReferences entityReferences) {
        this.likeRepository = likeRepository;
        this.entityReferences = entityReferences;
    }

    public void like(Moodboard moodboard, String likerUsername) {
        Long moodboardId = moodboard.getId();
        if (moodboardId == null) {
            return;
        }
        if (likeRepository.existsByMoodboard_IdAndLiker_Username(moodboardId, likerUsername)) {
            return;
        }
        User liker = entityReferences.requireUser(likerUsername);
        likeRepository.save(new MoodboardLike(moodboard, liker));
    }

    public void unlike(Long moodboardId, String likerUsername) {
        likeRepository.deleteByMoodboard_IdAndLiker_Username(moodboardId, likerUsername);
    }

    public List<String> getLikerUsernames(Long moodboardId) {
        return likeRepository.findByMoodboard_Id(moodboardId)
                .stream()
                .map(MoodboardLike::getLikerUsername)
                .toList();
    }

    public long countByMoodboardId(Long moodboardId) {
        return likeRepository.countByMoodboard_Id(moodboardId);
    }

    public List<Moodboard> getLikedMoodboards(String username) {
        return likeRepository.findByLiker_Username(username)
                .stream()
                .map(MoodboardLike::getMoodboard)
                .toList();
    }
}
