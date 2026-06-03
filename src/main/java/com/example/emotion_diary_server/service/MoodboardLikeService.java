package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.model.MoodboardLike;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.repository.MoodboardLikeRepository;
import com.example.emotion_diary_server.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Records and queries likes on public moodboards.
 */
@Service
public class MoodboardLikeService {

    private final MoodboardLikeRepository likeRepository;
    private final EntityReferences entityReferences;

    /**
     * @param likeRepository    persistence for like rows
     * @param entityReferences  resolves liker users
     */
    public MoodboardLikeService(MoodboardLikeRepository likeRepository, EntityReferences entityReferences) {
        this.likeRepository = likeRepository;
        this.entityReferences = entityReferences;
    }

    /**
     * Adds a like when the moodboard is persisted and the user has not already liked it.
     *
     * @param moodboard      target moodboard
     * @param likerUsername  username of the user liking the board
     */
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

    /**
     * Removes a like for the given user and moodboard.
     *
     * @param moodboardId     moodboard id
     * @param likerUsername   username of the user removing the like
     */
    public void unlike(Long moodboardId, String likerUsername) {
        likeRepository.deleteByMoodboard_IdAndLiker_Username(moodboardId, likerUsername);
    }

    /**
     * Lists usernames of users who liked a moodboard.
     *
     * @param moodboardId moodboard id
     * @return liker usernames
     */
    public List<String> getLikerUsernames(Long moodboardId) {
        return likeRepository.findByMoodboard_Id(moodboardId)
                .stream()
                .map(MoodboardLike::getLikerUsername)
                .toList();
    }

    /**
     * Counts likes for a moodboard.
     *
     * @param moodboardId moodboard id
     * @return number of likes
     */
    public long countByMoodboardId(Long moodboardId) {
        return likeRepository.countByMoodboard_Id(moodboardId);
    }

    /**
     * Returns moodboards liked by the given user.
     *
     * @param username liker username
     * @return liked moodboards
     */
    public List<Moodboard> getLikedMoodboards(String username) {
        return likeRepository.findByLiker_Username(username)
                .stream()
                .map(MoodboardLike::getMoodboard)
                .toList();
    }
}
