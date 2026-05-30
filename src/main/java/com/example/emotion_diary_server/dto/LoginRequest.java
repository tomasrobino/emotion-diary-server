package com.example.emotion_diary_server.dto;

import org.jspecify.annotations.Nullable;

public record LoginRequest(
        @Nullable String username,
        @Nullable String password
) {
}