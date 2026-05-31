package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.DiaryEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByOwner_UsernameAndEntryDateBetweenOrderByEntryDateAsc(
            String ownerUsername,
            LocalDate from,
            LocalDate to
    );

    Optional<DiaryEntry> findByOwner_UsernameAndEntryDate(String ownerUsername, LocalDate entryDate);

    @Transactional
    void deleteByOwner_Username(String ownerUsername);
}
