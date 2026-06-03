package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import org.springframework.stereotype.Service;

/**
 * Evaluates whether the current principal may access a moodboard.
 * Access is granted when the user is the owner, the moodboard is public,
 * or an explicit {@link MoodboardPermission} exists for that user.
 */
@Service("moodboardAccess")
public class MoodboardAccessService {

    private final MoodboardPermissionRepository permissionRepository;
    private final MoodboardRepository moodboardRepository;

    public MoodboardAccessService(
            MoodboardPermissionRepository permissionRepository,
            MoodboardRepository moodboardRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.moodboardRepository = moodboardRepository;
    }

    /**
     * @param moodboardId    the ID of the moodboard being accessed
     * @param ownerUsername  the owner of the moodboard
     * @param principalName  the username of the currently authenticated user
     * @return true if access should be granted
     */
    public boolean canAccess(Long moodboardId, String ownerUsername, String principalName) {
        if (principalName.equals(ownerUsername)) {
            return true;
        }

        if (moodboardRepository.findById(moodboardId).map(Moodboard::isPublic).orElse(false)) {
            return true;
        }

        return permissionRepository.existsByMoodboard_IdAndPermitted_Username(moodboardId, principalName);
    }
}
