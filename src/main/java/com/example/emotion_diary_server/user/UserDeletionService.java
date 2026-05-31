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

@Service
public class UserDeletionService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiaryEntryRepository diaryEntryRepository;
    private final MoodboardService moodboardService;
    private final MoodboardLikeRepository moodboardLikeRepository;
    private final MoodboardPermissionRepository moodboardPermissionRepository;
    private final TokenRevocationService tokenRevocationService;

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
