package com.example.emotion_diary_server.dto;

import java.util.List;

public record QuizTemplateDto(
        long id,
        String question,
        String type,
        List<String> options
) {
}
