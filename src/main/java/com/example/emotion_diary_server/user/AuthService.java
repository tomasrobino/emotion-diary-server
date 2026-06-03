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

/**
 * Handles user registration, logout, password changes, and account deletion orchestration.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;
    private final UserDeletionService userDeletionService;

    /**
     * @param userRepository           repository for user persistence
     * @param passwordEncoder            encoder for password hashing and verification
     * @param jwtService                 service that issues JWT access tokens
     * @param jwtProperties              JWT configuration including token lifetime
     * @param tokenRevocationService     service that revokes tokens on logout
     * @param userDeletionService        service that performs cascading account deletion
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            TokenRevocationService tokenRevocationService,
            UserDeletionService userDeletionService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenRevocationService = tokenRevocationService;
        this.userDeletionService = userDeletionService;
    }

    /**
     * Registers a new user and returns a JWT for immediate authentication.
     *
     * @param username desired username (normalized to lowercase)
     * @param password plaintext password (minimum six characters)
     * @return access token and expiration metadata
     * @throws UsernameAlreadyExistsException if the username is already taken
     * @throws IllegalArgumentException       if username or password validation fails
     */
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

    /**
     * Revokes the given JWT so it can no longer be used.
     *
     * @param token bearer token to invalidate
     */
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

    /**
     * Changes the authenticated user's password after verifying the current one.
     *
     * @param username        username of the account to update
     * @param currentPassword current plaintext password
     * @param newPassword     new plaintext password (minimum six characters)
     * @throws org.springframework.security.authentication.BadCredentialsException if the current password is wrong
     * @throws IllegalArgumentException                                              if the user is not found or the new password is invalid
     */
    @Transactional
    public void changePassword(String username, @Nullable String currentPassword, @Nullable String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid current password");
        }
        validatePassword(newPassword);
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(newPassword)));
        userRepository.save(user);
    }

    /**
     * Deletes the authenticated user's account and associated data.
     *
     * @param username username of the account to delete
     * @param password plaintext password for confirmation
     * @param token    optional JWT to revoke after deletion
     * @throws org.springframework.security.authentication.BadCredentialsException if the password is wrong
     * @throws IllegalArgumentException                                              if the user is not found
     */
    @Transactional
    public void deleteAccount(String username, @Nullable String password, @Nullable String token) {
        userDeletionService.deleteAccount(username, password, token);
    }
}
