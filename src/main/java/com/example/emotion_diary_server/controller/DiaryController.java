package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.DiaryEntryRequestDto;
import com.example.emotion_diary_server.dto.DiaryEntryResponseDto;
import com.example.emotion_diary_server.model.DiaryEntry;
import com.example.emotion_diary_server.service.DiaryEntryService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST endpoints for per-user diary entries keyed by calendar date.
 */
@Tag(name = "Diary")
@RestController
public class DiaryController {

    private final DiaryEntryService diaryEntryService;

    /**
     * @param diaryEntryService diary persistence service
     */
    public DiaryController(DiaryEntryService diaryEntryService) {
        this.diaryEntryService = diaryEntryService;
    }

    /**
     * GET /{user}/diary/entries — list diary entries in an inclusive date range.
     * <p>
     * Requires authenticated user matching {@code user} path segment.
     *
     * @param user owner username (must match principal)
     * @param from start date (ISO-8601 date)
     * @param to   end date (ISO-8601 date)
     * @return 200 OK with list of {@link DiaryEntryResponseDto}
     */
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

    /**
     * GET /{user}/diary/entries/{date} — fetch a single diary entry by date.
     * <p>
     * Requires authenticated user matching {@code user}.
     *
     * @param user owner username
     * @param date entry date (ISO-8601 date)
     * @return 200 OK with entry, or 404 if none exists for that date
     */
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

    /**
     * POST /{user}/diary/entries — create or update the entry for the date in the request body.
     * <p>
     * Requires authenticated user matching {@code user}.
     *
     * @param user    owner username
     * @param request mood, note, and entry date
     * @return 200 OK with saved {@link DiaryEntryResponseDto}
     */
    @PostMapping("/{user}/diary/entries")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<DiaryEntryResponseDto> upsertEntry(
            @PathVariable String user,
            @RequestBody DiaryEntryRequestDto request
    ) {
        DiaryEntry saved = diaryEntryService.upsert(user, request);
        return ResponseEntity.ok(DiaryEntryResponseDto.from(saved));
    }

    /**
     * DELETE /{user}/diary/entries/{date} — remove the entry for the given date.
     * <p>
     * Requires authenticated user matching {@code user}.
     *
     * @param user owner username
     * @param date entry date (ISO-8601 date)
     * @return 204 No Content when deleted, 404 when no entry existed
     */
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
