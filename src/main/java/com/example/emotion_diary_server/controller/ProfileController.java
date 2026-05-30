package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.ChangePasswordRequestDto;
import com.example.emotion_diary_server.user.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
