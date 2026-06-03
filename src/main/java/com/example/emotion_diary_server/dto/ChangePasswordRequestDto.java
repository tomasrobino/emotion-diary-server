package com.example.emotion_diary_server.dto;

/**
 * Request body for changing the authenticated user's password.
 */
public record ChangePasswordRequestDto(
        /** Current plaintext password for verification. */
        String currentPassword,
        /** New plaintext password; at least six characters. */
        String newPassword
) {
}
