package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.ChangePasswordRequestDto;
import com.example.emotion_diary_server.dto.DeleteAccountRequestDto;
import com.example.emotion_diary_server.user.AuthService;
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

@RestController
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @PatchMapping("/{user}/profile/password")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> changePassword(
            @PathVariable String user,
            @RequestBody ChangePasswordRequestDto request
    ) {
        authService.changePassword(user, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

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
