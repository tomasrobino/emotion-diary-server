package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;

/**
 * Request body for user login or registration.
 */
public record LoginRequest(
        /** Username; letters, numbers, underscores, and hyphens only (max 50 characters). */
        @Nullable String username,
        /** Plaintext password; at least six characters. */
        @Nullable String password
) {
}
