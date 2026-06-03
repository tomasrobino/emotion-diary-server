package com.example.emotion_diary_server.user;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.DiaryEntryRepository;
import com.example.emotion_diary_server.repository.MoodboardLikeRepository;
import com.example.emotion_diary_server.security.MoodboardPermissionRepository;
import com.example.emotion_diary_server.security.TokenRevocationService;
import com.example.emotion_diary_server.service.MoodboardService;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Cascading deletion of a user account and all owned or related data.
 */
@Service
public class UserDeletionService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiaryEntryRepository diaryEntryRepository;
    private final MoodboardService moodboardService;
    private final MoodboardLikeRepository moodboardLikeRepository;
    private final MoodboardPermissionRepository moodboardPermissionRepository;
    private final TokenRevocationService tokenRevocationService;

    /**
     * @param userRepository                 repository for user entities
     * @param passwordEncoder                encoder used to verify the deletion password
     * @param diaryEntryRepository           repository for diary entries owned by the user
     * @param moodboardService               service for loading and deleting moodboards
     * @param moodboardLikeRepository        repository for likes given by the user
     * @param moodboardPermissionRepository  repository for moodboard permissions granted to the user
     * @param tokenRevocationService         service that revokes JWTs after deletion
     */
    public UserDeletionService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DiaryEntryRepository diaryEntryRepository,
            MoodboardService moodboardService,
            MoodboardLikeRepository moodboardLikeRepository,
            MoodboardPermissionRepository moodboardPermissionRepository,
            TokenRevocationService tokenRevocationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.diaryEntryRepository = diaryEntryRepository;
        this.moodboardService = moodboardService;
        this.moodboardLikeRepository = moodboardLikeRepository;
        this.moodboardPermissionRepository = moodboardPermissionRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    /**
     * Permanently deletes the user after password confirmation, including diary entries,
     * owned moodboards, likes, permissions, and optional token revocation.
     *
     * @param username username of the account to delete
     * @param password plaintext password for confirmation
     * @param token    optional JWT to revoke; may be {@code null}
     * @throws org.springframework.security.authentication.BadCredentialsException if the password is wrong
     * @throws IllegalArgumentException                                              if the user is not found
     */
    @Transactional
    public void deleteAccount(String username, @Nullable String password, @Nullable String token) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid password");
        }

        diaryEntryRepository.deleteByOwner_Username(username);

        for (Moodboard moodboard : moodboardService.findByOwnerUsername(username)) {
            moodboardService.deleteById(Objects.requireNonNull(moodboard.getId()));
        }

        moodboardLikeRepository.deleteByLiker_Username(username);
        moodboardPermissionRepository.deleteByPermitted_Username(username);

        if (token != null) {
            tokenRevocationService.revoke(token);
        }

        userRepository.delete(user);
    }
}
