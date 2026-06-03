package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.ChangePasswordRequestDto;
import com.example.emotion_diary_server.dto.DeleteAccountRequestDto;
import com.example.emotion_diary_server.user.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the authenticated user's profile (password and account deletion).
 */
@Tag(name = "Profile")
@RestController
public class ProfileController {

    private final AuthService authService;

    /**
     * @param authService account and credential operations
     */
    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * PATCH /{user}/profile/password — change the account password.
     * <p>
     * Requires authenticated user matching {@code user}. Current password must be verified.
     *
     * @param user    owner username
     * @param request current and new password
     * @return 204 No Content on success
     * @throws IllegalArgumentException when current password is wrong (400 via advice)
     */
    @PatchMapping("/{user}/profile/password")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> changePassword(
            @PathVariable String user,
            @RequestBody ChangePasswordRequestDto request
    ) {
        authService.changePassword(user, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /{user}/profile — permanently delete the account and related data.
     * <p>
     * Requires authenticated user matching {@code user}. Password must be confirmed.
     * Revokes the Bearer token from the request when present.
     *
     * @param user         owner username
     * @param request      account password for confirmation
     * @param httpRequest  used to extract Bearer token for revocation
     * @return 204 No Content on success
     * @throws IllegalArgumentException when password is wrong (400 via advice)
     */
    @DeleteMapping("/{user}/profile")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable String user,
            @RequestBody DeleteAccountRequestDto request,
            HttpServletRequest httpRequest
    ) {
        authService.deleteAccount(user, request.password(), extractBearerToken(httpRequest));
        return ResponseEntity.noContent().build();
    }

    private static @Nullable String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
