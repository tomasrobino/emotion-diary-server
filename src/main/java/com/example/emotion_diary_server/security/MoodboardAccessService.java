package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import org.springframework.stereotype.Service;

/**
 * Used to check moodboard access per moodboard.
 * Access is granted when any of these holds:
 *  1. The authenticated user is the owner
 *  2. The moodboard is marked public
 *  3. The authenticated user has been explicitly granted access to that moodboard
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

        return permissionRepository.existsByMoodboardIdAndPermittedUsername(moodboardId, principalName);
    }
}
