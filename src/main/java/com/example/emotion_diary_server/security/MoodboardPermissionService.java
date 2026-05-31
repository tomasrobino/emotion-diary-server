package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void revokeAccess(Long moodboardId, String revokeFrom) {
        permissionRepository.deleteByMoodboard_IdAndPermitted_Username(
                moodboardId,
                revokeFrom.trim().toLowerCase()
        );
    }

    public List<String> listPermittedUsernames(Long moodboardId) {
        return permissionRepository.findByMoodboard_Id(moodboardId)
                .stream()
                .map(MoodboardPermission::getPermittedUsername)
                .toList();
    }
}
