package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages explicit moodboard access grants: grant, revoke, and list permitted users.
 */
@Service
public class MoodboardPermissionService {

    private final MoodboardPermissionRepository permissionRepository;
    private final EntityReferences entityReferences;
    private final UserService userService;

    public MoodboardPermissionService(
            MoodboardPermissionRepository permissionRepository,
            EntityReferences entityReferences,
            UserService userService
    ) {
        this.permissionRepository = permissionRepository;
        this.entityReferences = entityReferences;
        this.userService = userService;
    }

    /**
     * Grants another user access to a moodboard owned by {@code ownerUsername}.
     *
     * @param moodboard      moodboard to share
     * @param ownerUsername  owner performing the grant
     * @param grantTo        username to grant (case-insensitive)
     * @throws IllegalArgumentException if the target user is invalid or is the owner
     */
    public void grantAccess(Moodboard moodboard, String ownerUsername, String grantTo) {
        String grantToUsername = grantTo.trim().toLowerCase();
        if (grantToUsername.isEmpty()) {
            throw new IllegalArgumentException("Se requiere un nombre de usuario");
        }
        if (grantToUsername.equals(ownerUsername)) {
            throw new IllegalArgumentException("No puedes darte acceso a ti mismo");
        }
        if (!userService.existsByUsernameIgnoreCase(grantToUsername)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        Long moodboardId = moodboard.getId();
        if (moodboardId != null
                && !permissionRepository.existsByMoodboard_IdAndPermitted_Username(moodboardId, grantToUsername)) {
            User owner = entityReferences.requireUser(ownerUsername);
            User permitted = entityReferences.requireUser(grantToUsername);
            permissionRepository.save(new MoodboardPermission(moodboard, owner, permitted));
        }
    }

    /**
     * Revokes a user's explicit access to a moodboard.
     *
     * @param moodboardId moodboard identifier
     * @param revokeFrom  username to revoke (trimmed, lowercased)
     */
    public void revokeAccess(Long moodboardId, String revokeFrom) {
        permissionRepository.deleteByMoodboard_IdAndPermitted_Username(
                moodboardId,
                revokeFrom.trim().toLowerCase()
        );
    }

    /**
     * Returns usernames that have been explicitly granted access to a moodboard.
     *
     * @param moodboardId moodboard identifier
     * @return permitted usernames, in repository order
     */
    public List<String> listPermittedUsernames(Long moodboardId) {
        return permissionRepository.findByMoodboard_Id(moodboardId)
                .stream()
                .map(MoodboardPermission::getPermittedUsername)
                .toList();
    }
}
