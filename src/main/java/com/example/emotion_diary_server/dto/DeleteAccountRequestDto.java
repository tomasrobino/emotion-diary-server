package com.example.emotion_diary_server.dto;

/**
 * Request body for permanently deleting the authenticated user's account.
 */
public record DeleteAccountRequestDto(
        /** Plaintext password confirming account ownership. */
        String password
) {
}
