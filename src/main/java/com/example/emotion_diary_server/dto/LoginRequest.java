package com.example.emotion_diary_server.dto;

public record LoginRequest(
        String username,
        String password
) {
}
