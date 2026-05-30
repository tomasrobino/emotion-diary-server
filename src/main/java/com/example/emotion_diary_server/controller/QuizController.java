package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.QuizResponseRequestDto;
import com.example.emotion_diary_server.dto.QuizTodayResponseDto;
import com.example.emotion_diary_server.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/quiz/today")
    public ResponseEntity<QuizTodayResponseDto> getTodayQuiz(Authentication authentication) {
        return ResponseEntity.ok(quizService.getTodayQuiz(authentication.getName()));
    }

    @PostMapping("/{user}/quiz/responses")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<Void> submitResponse(
            @PathVariable String user,
            @RequestBody QuizResponseRequestDto request
    ) {
        quizService.submitResponse(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
