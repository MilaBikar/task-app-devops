package com.example.analyticsservice.service;

import com.example.analyticsservice.dto.AnalyticsDTO;
import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsDTO recordEvent(AnalyticsDTO analyticsDTO) {
        Analytics analytics = new Analytics();
        analytics.setEventType(analyticsDTO.getEventType());
        analytics.setResourceType(analyticsDTO.getResourceType());
        analytics.setResourceId(analyticsDTO.getResourceId());
        analytics.setDetails(analyticsDTO.getDetails());


        Analytics savedAnalytics = analyticsRepository.save(analytics);
        return mapToDTO(savedAnalytics);
    }

    public List<AnalyticsDTO> getAllEvents() {
        return analyticsRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AnalyticsDTO> getEventsByType(String eventType) {
        return analyticsRepository.findByEventType(eventType).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AnalyticsDTO mapToDTO(Analytics analytics) {
        return new AnalyticsDTO(
                analytics.getId(),
                analytics.getEventType(),
                analytics.getResourceType(),
                analytics.getResourceId(),
                analytics.getDetails(),
                analytics.getCreatedAt()
        );
    }
}
