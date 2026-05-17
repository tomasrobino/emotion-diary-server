package com.example.emotion_diary_server.security;

import org.springframework.stereotype.Service;

/**
 * Used in @PreAuthorize expressions to check moodboard access.
 * Two cases are allowed:
 *  1. The authenticated user IS the path's {user}  (owner access)
 *  2. The authenticated user has been explicitly granted access to that {user}'s moodboards
 */
@Service("moodboardAccess")
public class MoodboardAccessService {

    private final MoodboardPermissionRepository permissionRepository;

    public MoodboardAccessService(MoodboardPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * @param requestedUser  the {user} path variable from the URL
     * @param principalName  the username of the currently authenticated user
     * @return true if access should be granted
     */
    public boolean canAccess(String requestedUser, String principalName) {
        // Case 1: user is accessing their own moodboards
        if (principalName.equals(requestedUser)) {
            return true;
        }

        // Case 2: another user has been explicitly granted access
        return permissionRepository.existsByOwnerUsernameAndPermittedUsername(
                requestedUser, principalName
        );
    }
}
