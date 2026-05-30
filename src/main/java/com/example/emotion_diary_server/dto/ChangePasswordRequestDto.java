package com.example.emotion_diary_server.dto;

public record ChangePasswordRequestDto(
        String currentPassword,
        String newPassword
) {
}
