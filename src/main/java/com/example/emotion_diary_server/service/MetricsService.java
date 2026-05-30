package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.dto.MetricsResponseDto;
import com.example.emotion_diary_server.model.DiaryEntry;
import com.example.emotion_diary_server.repository.DiaryEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MetricsService {

    private final DiaryEntryRepository diaryEntryRepository;

    public MetricsService(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    public MetricsResponseDto computeMetrics(String ownerUsername, String period) {
        int days = parsePeriod(period);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1L);

        List<DiaryEntry> entries = diaryEntryRepository
                .findByOwnerUsernameAndEntryDateBetweenOrderByEntryDateAsc(ownerUsername, from, to);

        double average = entries.isEmpty()
                ? 0.0
                : entries.stream().mapToInt(DiaryEntry::getMoodScore).average().orElse(0.0);

        List<MetricsResponseDto.MoodTrendPointDto> trend = entries.stream()
                .map(e -> new MetricsResponseDto.MoodTrendPointDto(
                        e.getEntryDate().toString(),
                        e.getMoodScore()
                ))
                .toList();

        return new MetricsResponseDto(
                period,
                Math.round(average * 100.0) / 100.0,
                computeStreak(ownerUsername),
                entries.size(),
                trend
        );
    }

    private int computeStreak(String ownerUsername) {
        LocalDate cursor = LocalDate.now();
        int streak = 0;
        while (true) {
            if (diaryEntryRepository.findByOwnerUsernameAndEntryDate(ownerUsername, cursor).isEmpty()) {
                break;
            }
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private static int parsePeriod(String period) {
        return switch (period) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            default -> throw new IllegalArgumentException("period must be 7d, 30d, or 90d");
        };
    }
}
