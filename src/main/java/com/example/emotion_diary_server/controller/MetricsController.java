package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.MetricsResponseDto;
import com.example.emotion_diary_server.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/{user}/metrics")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<MetricsResponseDto> getMetrics(
            @PathVariable String user,
            @RequestParam(defaultValue = "30d") String period
    ) {
        return ResponseEntity.ok(metricsService.computeMetrics(user, period));
    }
}
