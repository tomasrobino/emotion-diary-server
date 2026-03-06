package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.model.Note;
import com.example.emotion_diary_server.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Note> getAllNotes() {
        return (List<Note>) noteService.findAll();
    }

    @PostMapping
    public Note createNote(@RequestBody Note note) {
        noteService.save(note);
        return note;
    }
}