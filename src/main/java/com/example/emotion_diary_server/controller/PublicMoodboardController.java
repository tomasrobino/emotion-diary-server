package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.PublicMoodboardFeedItemDto;
import com.example.emotion_diary_server.dto.PublicMoodboardsPageDto;
import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.service.MoodboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PublicMoodboardController {

    private static final int DEFAULT_SIZE = 24;
    private static final int MAX_SIZE = 50;

    private final MoodboardService moodboardService;

    public PublicMoodboardController(MoodboardService moodboardService) {
        this.moodboardService = moodboardService;
    }

    /**
     * GET /public/moodboards?page=0&size=24
     * Returns paginated public moodboards from other users.
     */
    @GetMapping("/public/moodboards")
    public ResponseEntity<PublicMoodboardsPageDto> getPublicMoodboards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            Authentication authentication
    ) {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }

        String principalName = authentication.getName();
        Page<Moodboard> result = moodboardService.findPublicByOtherUsers(
                principalName,
                PageRequest.of(page, size)
        );

        List<PublicMoodboardFeedItemDto> items = result.getContent()
                .stream()
                .map(PublicMoodboardFeedItemDto::from)
                .toList();

        return ResponseEntity.ok(new PublicMoodboardsPageDto(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        ));
    }
}
