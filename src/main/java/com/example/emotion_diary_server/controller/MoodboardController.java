package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.service.MoodboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moodboards")
public class MoodboardController {

    private final MoodboardService moodboardService;

    public MoodboardController(MoodboardService moodboardService) {
        this.moodboardService = moodboardService;
    }

    @GetMapping
    public List<Moodboard> getAllNotes() {
        return (List<Moodboard>) moodboardService.findAll();
    }

    @PostMapping
    public Moodboard createNote(@RequestBody Moodboard moodboard) {
        moodboardService.save(moodboard);
        return moodboard;
    }
}