package com.example.emotion_diary_server.user;

import com.example.emotion_diary_server.config.JwtProperties;
import com.example.emotion_diary_server.dto.LoginResponse;
import com.example.emotion_diary_server.security.JwtService;
import com.example.emotion_diary_server.security.TokenRevocationService;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            TokenRevocationService tokenRevocationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Transactional
    public LoginResponse register(@Nullable String username, @Nullable String password) {
        String normalizedUsername = validateAndNormalizeUsername(username);
        validatePassword(password);

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(password)));
        userRepository.save(user);

        String token = jwtService.generateToken(normalizedUsername);
        return new LoginResponse(token, jwtProperties.expirationMs());
    }

    public void logout(String token) {
        tokenRevocationService.revoke(token);
    }

    private String validateAndNormalizeUsername(@Nullable String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        String normalized = username.trim();
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("Username must be at most 50 characters");
        }
        if (!normalized.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Username may only contain letters, numbers, underscores, and hyphens");
        }
        return normalized.toLowerCase();
    }

    private void validatePassword(@Nullable String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }
}
