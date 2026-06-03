package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.dto.DiaryEntryRequestDto;
import com.example.emotion_diary_server.model.DiaryEntry;
import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.repository.DiaryEntryRepository;
import com.example.emotion_diary_server.user.User;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Diary entry queries and upserts for a single owner, including optional moodboard links.
 */
@Service
public class DiaryEntryService {

    private final DiaryEntryRepository diaryEntryRepository;
    private final EntityReferences entityReferences;

    /**
     * @param diaryEntryRepository repository for diary entries
     * @param entityReferences     resolves users and moodboards by reference
     */
    public DiaryEntryService(DiaryEntryRepository diaryEntryRepository, EntityReferences entityReferences) {
        this.diaryEntryRepository = diaryEntryRepository;
        this.entityReferences = entityReferences;
    }

    /**
     * Returns entries for the owner between {@code from} and {@code to} (inclusive), ordered by date ascending.
     *
     * @param ownerUsername owner username
     * @param from          start date (inclusive)
     * @param to            end date (inclusive)
     * @return matching diary entries
     * @throws IllegalArgumentException if {@code from} is after {@code to}
     */
    public List<DiaryEntry> findInRange(String ownerUsername, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        return diaryEntryRepository.findByOwner_UsernameAndEntryDateBetweenOrderByEntryDateAsc(
                ownerUsername, from, to
        );
    }

    /**
     * Loads the entry for a single calendar day, if present.
     *
     * @param ownerUsername owner username
     * @param date          entry date
     * @return the entry, or {@code null} if none exists for that date
     */
    public @Nullable DiaryEntry findByDate(String ownerUsername, LocalDate date) {
        return diaryEntryRepository.findByOwner_UsernameAndEntryDate(ownerUsername, date).orElse(null);
    }

    /**
     * Creates or updates the diary entry for the request's entry date.
     *
     * @param ownerUsername authenticated owner username
     * @param request       entry payload
     * @return the persisted entry
     * @throws IllegalArgumentException if mood score is out of range, entry date is missing,
     *                                  or linked moodboard does not belong to the owner
     */
    @Transactional
    public DiaryEntry upsert(String ownerUsername, DiaryEntryRequestDto request) {
        validateMoodScore(request.moodScore());
        LocalDate entryDate = Objects.requireNonNull(request.entryDate(), "entryDate is required");
        User owner = entityReferences.requireUser(ownerUsername);
        Moodboard linkedMoodboard = resolveLinkedMoodboard(ownerUsername, request.linkedMoodboardId());

        DiaryEntry entry = diaryEntryRepository
                .findByOwner_UsernameAndEntryDate(ownerUsername, entryDate)
                .orElseGet(DiaryEntry::new);

        Instant now = Instant.now();
        if (entry.getId() == null) {
            entry.setCreatedAt(now);
        }
        entry.setOwner(owner);
        entry.setEntryDate(entryDate);
        entry.setMoodScore(request.moodScore());
        entry.setTextNote(normalizeText(request.textNote()));
        entry.setLinkedMoodboard(linkedMoodboard);
        entry.setReminderAt(request.reminderAt());
        entry.setUpdatedAt(now);

        return diaryEntryRepository.save(entry);
    }

    /**
     * Deletes the entry for the given date when it exists.
     *
     * @param ownerUsername owner username
     * @param date          entry date
     * @return {@code true} if an entry was deleted, {@code false} if none existed
     */
    @Transactional
    public boolean deleteByDate(String ownerUsername, LocalDate date) {
        return diaryEntryRepository.findByOwner_UsernameAndEntryDate(ownerUsername, date)
                .map(entry -> {
                    diaryEntryRepository.delete(entry);
                    return true;
                })
                .orElse(false);
    }

    private @Nullable Moodboard resolveLinkedMoodboard(String ownerUsername, @Nullable Long linkedMoodboardId) {
        if (linkedMoodboardId == null) {
            return null;
        }
        Moodboard moodboard = entityReferences.requireMoodboard(linkedMoodboardId);
        if (!ownerUsername.equals(moodboard.getOwnerUsername())) {
            throw new IllegalArgumentException("linkedMoodboardId does not belong to the current user");
        }
        return moodboard;
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
