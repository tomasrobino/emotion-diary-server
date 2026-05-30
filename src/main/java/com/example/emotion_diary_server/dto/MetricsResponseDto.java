package com.example.emotion_diary_server.dto;

import java.util.List;

public record MetricsResponseDto(
        String period,
        double averageMood,
        int entryStreak,
        int totalEntries,
        List<MoodTrendPointDto> trend
) {
    public record MoodTrendPointDto(String date, int moodScore) {
    }
}
