package com.example.emotion_diary_server.repository;

import com.example.emotion_diary_server.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {}
