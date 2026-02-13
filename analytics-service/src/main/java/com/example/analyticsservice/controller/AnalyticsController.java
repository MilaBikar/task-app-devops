package com.example.analyticsservice.controller;

import com.example.analyticsservice.dto.AnalyticsDTO;
import com.example.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalyticsDTO recordEvent(@RequestBody AnalyticsDTO analyticsDTO) {
        return analyticsService.recordEvent(analyticsDTO);
    }

    @GetMapping
    public List<AnalyticsDTO> getAllEvents() {
        return analyticsService.getAllEvents();
    }

    @GetMapping("/type/{eventType}")
    public List<AnalyticsDTO> getEventsByType(@PathVariable String eventType) {
        return analyticsService.getEventsByType(eventType);
    }
}
