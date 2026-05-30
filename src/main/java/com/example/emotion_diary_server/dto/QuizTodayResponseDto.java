package com.example.emotion_diary_server.dto;

import java.util.List;
import java.util.Map;

public record QuizTodayResponseDto(
        List<QuizTemplateDto> questions,
        boolean completedToday
) {
}
