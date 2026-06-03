package com.example.emotion_diary_server.controller;

import com.example.emotion_diary_server.dto.MetricsResponseDto;
import com.example.emotion_diary_server.service.MetricsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for aggregated diary mood metrics.
 */
@Tag(name = "Metrics")
@RestController
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * @param metricsService metrics computation
     */
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * GET /{user}/metrics — compute mood trends and summaries for a time period.
     * <p>
     * Requires authenticated user matching {@code user}. Default period is {@code 30d}.
     *
     * @param user   owner username
     * @param period lookback window (e.g. {@code 7d}, {@code 30d}, {@code 90d})
     * @return 200 OK with {@link MetricsResponseDto}
     */
    @GetMapping("/{user}/metrics")
    @PreAuthorize("#user == authentication.name")
    public ResponseEntity<MetricsResponseDto> getMetrics(
            @PathVariable String user,
            @RequestParam(defaultValue = "30d") String period
    ) {
        return ResponseEntity.ok(metricsService.computeMetrics(user, period));
    }
}
