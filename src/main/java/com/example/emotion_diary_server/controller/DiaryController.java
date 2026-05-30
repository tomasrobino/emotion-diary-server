package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.DiaryEntryRequestDto;
import com.example.emotion_diary_server.dto.DiaryEntryResponseDto;
import com.example.emotion_diary_server.model.DiaryEntry;
import com.example.emotion_diary_server.service.DiaryEntryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {

    private final DiaryEntryService diaryEntryService;

    public DiaryController(DiaryEntryService diaryEntryService) {
        this.diaryEntryService = diaryEntryService;
    }

    @GetMapping("/{user}/diary/entries")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<List<DiaryEntryResponseDto>> listEntries(
            @PathVariable String user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<DiaryEntryResponseDto> entries = diaryEntryService.findInRange(user, from, to)
                .stream()
                .map(DiaryEntryResponseDto::from)
                .toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{user}/diary/entries/{date}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<DiaryEntryResponseDto> getEntry(
            @PathVariable String user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DiaryEntry entry = diaryEntryService.findByDate(user, date);
        if (entry == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(DiaryEntryResponseDto.from(entry));
    }

    @PostMapping("/{user}/diary/entries")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<DiaryEntryResponseDto> upsertEntry(
            @PathVariable String user,
            @RequestBody DiaryEntryRequestDto request
    ) {
        DiaryEntry saved = diaryEntryService.upsert(user, request);
        return ResponseEntity.ok(DiaryEntryResponseDto.from(saved));
    }

    @DeleteMapping("/{user}/diary/entries/{date}")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable String user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (!diaryEntryService.deleteByDate(user, date)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
