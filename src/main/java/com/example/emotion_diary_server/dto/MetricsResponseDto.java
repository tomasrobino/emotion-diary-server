package com.example.emotion_diary_server.dto;

import java.util.List;

/**
 * Aggregated diary metrics for a selected time period.
 */
public record MetricsResponseDto(
        /** Period label (e.g. {@code week}, {@code month}). */
        String period,
        /** Average mood score across entries in the period. */
        double averageMood,
        /** Longest streak of consecutive days with entries. */
        int entryStreak,
        /** Total number of diary entries in the period. */
        int totalEntries,
        /** Daily mood scores used to render a trend chart. */
        List<MoodTrendPointDto> trend
) {
    /**
     * Single data point on a mood trend timeline.
     */
    public record MoodTrendPointDto(
            /** ISO-8601 date string ({@code yyyy-MM-dd}). */
            String date,
            /** Mood score on that date (1–10). */
            int moodScore
    ) {
    }
}
