package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.DiaryEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence access for {@link DiaryEntry} rows.
 */
public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    /**
     * Returns entries for an owner within an inclusive date range, ordered by date ascending.
     *
     * @param ownerUsername owner username
     * @param from          start date (inclusive)
     * @param to            end date (inclusive)
     * @return matching entries, possibly empty
     */
    List<DiaryEntry> findByOwner_UsernameAndEntryDateBetweenOrderByEntryDateAsc(
            String ownerUsername,
            LocalDate from,
            LocalDate to
    );

    /**
     * Finds the entry for a specific owner and calendar day, if present.
     *
     * @param ownerUsername owner username
     * @param entryDate     calendar day
     * @return the entry when it exists
     */
    Optional<DiaryEntry> findByOwner_UsernameAndEntryDate(String ownerUsername, LocalDate entryDate);

    /**
     * Removes all diary entries owned by the given user.
     *
     * @param ownerUsername owner username
     */
    @Transactional
    void deleteByOwner_Username(String ownerUsername);
}
