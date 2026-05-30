package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByOwnerUsernameAndEntryDateBetweenOrderByEntryDateAsc(
            String ownerUsername,
            LocalDate from,
            LocalDate to
    );

    Optional<DiaryEntry> findByOwnerUsernameAndEntryDate(String ownerUsername, LocalDate entryDate);
}
