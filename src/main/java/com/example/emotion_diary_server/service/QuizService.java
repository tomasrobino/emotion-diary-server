package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.dto.QuizResponseRequestDto;
import com.example.emotion_diary_server.dto.QuizTemplateDto;
import com.example.emotion_diary_server.dto.QuizTodayResponseDto;
import com.example.emotion_diary_server.model.QuizResponse;
import com.example.emotion_diary_server.model.QuizTemplate;
import com.example.emotion_diary_server.repository.QuizResponseRepository;
import com.example.emotion_diary_server.repository.QuizTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private final QuizTemplateRepository templateRepository;
    private final QuizResponseRepository responseRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public QuizService(
            QuizTemplateRepository templateRepository,
            QuizResponseRepository responseRepository
    ) {
        this.templateRepository = templateRepository;
        this.responseRepository = responseRepository;
    }

    public QuizTodayResponseDto getTodayQuiz(String ownerUsername) {
        LocalDate today = LocalDate.now();
        boolean completed = responseRepository
                .findByOwnerUsernameAndResponseDate(ownerUsername, today)
                .isPresent();
        List<QuizTemplateDto> questions = templateRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toDto)
                .toList();
        return new QuizTodayResponseDto(questions, completed);
    }

    @Transactional
    public void submitResponse(String ownerUsername, QuizResponseRequestDto request) {
        LocalDate today = LocalDate.now();
        if (responseRepository.findByOwnerUsernameAndResponseDate(ownerUsername, today).isPresent()) {
            throw new IllegalArgumentException("Ya has completado el quiz de hoy");
        }
        if (request.answers() == null || request.answers().isEmpty()) {
            throw new IllegalArgumentException("answers are required");
        }

        QuizResponse response = new QuizResponse();
        response.setOwnerUsername(ownerUsername);
        response.setResponseDate(today);
        response.setAnswers(serializeAnswers(request.answers()));
        response.setCreatedAt(Instant.now());
        responseRepository.save(response);
    }

    private QuizTemplateDto toDto(QuizTemplate template) {
        return new QuizTemplateDto(
                template.getId() != null ? template.getId() : 0L,
                template.getQuestion() != null ? template.getQuestion() : "",
                template.getType() != null ? template.getType() : "scale",
                parseOptions(template.getOptions())
        );
    }

    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String serializeAnswers(Map<String, String> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid answers format");
        }
    }
}
