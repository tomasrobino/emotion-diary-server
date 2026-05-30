package com.example.emotion_diary_server.dto;

import java.util.Map;

public record QuizResponseRequestDto(
        Map<String, String> answers
) {
}
