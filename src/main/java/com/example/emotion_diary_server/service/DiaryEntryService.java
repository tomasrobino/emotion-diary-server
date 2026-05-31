package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.dto.DiaryEntryRequestDto;
import com.example.emotion_diary_server.model.DiaryEntry;
import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.repository.DiaryEntryRepository;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class DiaryEntryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final MoodboardRepository moodboardRepository;

    public DiaryEntryService(
            DiaryEntryRepository diaryEntryRepository,
            MoodboardRepository moodboardRepository
    ) {
        this.diaryEntryRepository = diaryEntryRepository;
        this.moodboardRepository = moodboardRepository;
    }

    public List<DiaryEntry> findInRange(String ownerUsername, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        return diaryEntryRepository.findByOwnerUsernameAndEntryDateBetweenOrderByEntryDateAsc(
                ownerUsername, from, to
        );
    }

    public @Nullable DiaryEntry findByDate(String ownerUsername, LocalDate date) {
        return diaryEntryRepository.findByOwnerUsernameAndEntryDate(ownerUsername, date).orElse(null);
    }

    @Transactional
    public DiaryEntry upsert(String ownerUsername, DiaryEntryRequestDto request) {
        validateMoodScore(request.moodScore());
        LocalDate entryDate = Objects.requireNonNull(request.entryDate(), "entryDate is required");
        validateLinkedMoodboard(ownerUsername, request.linkedMoodboardId());

        DiaryEntry entry = diaryEntryRepository
                .findByOwnerUsernameAndEntryDate(ownerUsername, entryDate)
                .orElseGet(DiaryEntry::new);

        Instant now = Instant.now();
        if (entry.getId() == null) {
            entry.setCreatedAt(now);
        }
        entry.setOwnerUsername(ownerUsername);
        entry.setEntryDate(entryDate);
        entry.setMoodScore(request.moodScore());
        entry.setTextNote(normalizeText(request.textNote()));
        entry.setLinkedMoodboardId(request.linkedMoodboardId());
        entry.setReminderAt(request.reminderAt());
        entry.setUpdatedAt(now);

        return diaryEntryRepository.save(entry);
    }

    @Transactional
    public boolean deleteByDate(String ownerUsername, LocalDate date) {
        return diaryEntryRepository.findByOwnerUsernameAndEntryDate(ownerUsername, date)
                .map(entry -> {
                    diaryEntryRepository.delete(entry);
                    return true;
                })
                .orElse(false);
    }

    private void validateLinkedMoodboard(String ownerUsername, @Nullable Long linkedMoodboardId) {
        if (linkedMoodboardId == null) {
            return;
        }
        Moodboard moodboard = moodboardRepository.findById(linkedMoodboardId)
                .orElseThrow(() -> new IllegalArgumentException("linkedMoodboardId does not exist"));
        if (!ownerUsername.equals(moodboard.getOwnerUsername())) {
            throw new IllegalArgumentException("linkedMoodboardId does not belong to the current user");
        }
    }

    private static void validateMoodScore(int moodScore) {
        if (moodScore < 1 || moodScore > 5) {
            throw new IllegalArgumentException("moodScore must be between 1 and 5");
        }
    }

    private static @Nullable String normalizeText(@Nullable String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }
}
