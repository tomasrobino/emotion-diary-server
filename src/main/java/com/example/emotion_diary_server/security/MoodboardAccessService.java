package com.example.emotion_diary_server.security;

import org.springframework.stereotype.Service;

/**
 * Used to check moodboard access per moodboard.
 * Two cases are allowed:
 *  1. The authenticated user IS the owner
 *  2. The authenticated user has been explicitly granted access to that specific moodboard
 */
@Service("moodboardAccess")
public class MoodboardAccessService {

    private final MoodboardPermissionRepository permissionRepository;

    public MoodboardAccessService(MoodboardPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * @param moodboardId    the ID of the moodboard being accessed
     * @param ownerUsername  the owner of the moodboard
     * @param principalName  the username of the currently authenticated user
     * @return true if access should be granted
     */
    public boolean canAccess(Long moodboardId, String ownerUsername, String principalName) {
        // Case 1: user is the owner
        if (principalName.equals(ownerUsername)) {
            return true;
        }

        // Case 2: user has been explicitly granted access to this specific moodboard
        return permissionRepository.existsByMoodboardIdAndPermittedUsername(moodboardId, principalName);
    }
}
