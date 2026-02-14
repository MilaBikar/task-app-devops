package com.example.analyticsservice.service;

import com.example.analyticsservice.dto.AnalyticsDTO;
import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.repository.AnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UNIT TESTS - AnalyticsService")
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private AnalyticsDTO testAnalyticsDTO;
    private Analytics testAnalytics;

    @BeforeEach
    void setUp() {
        testAnalyticsDTO = new AnalyticsDTO(
                null,
                "USER_LOGIN",
                "USER",
                1L,
                "User logged in",
                null);

        testAnalytics = new Analytics();
        testAnalytics.setId(1L);
        testAnalytics.setEventType("USER_LOGIN");
        testAnalytics.setResourceType("USER");
        testAnalytics.setResourceId(1L);
        testAnalytics.setDetails("User logged in");
        testAnalytics.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should record event successfully")
    void shouldRecordEventSuccessfully() {
        when(analyticsRepository.save(any(Analytics.class))).thenReturn(testAnalytics);

        AnalyticsDTO result = analyticsService.recordEvent(testAnalyticsDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("USER_LOGIN", result.getEventType());
        assertNotNull(result.getCreatedAt());

        verify(analyticsRepository, times(1)).save(any(Analytics.class));
    }

    @Test
    @DisplayName("Should get all events successfully")
    void shouldGetAllEventsSuccessfully() {
        when(analyticsRepository.findAll()).thenReturn(Arrays.asList(testAnalytics));

        List<AnalyticsDTO> result = analyticsService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER_LOGIN", result.get(0).getEventType());

        verify(analyticsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get events by type successfully")
    void shouldGetEventsByTypeSuccessfully() {
        when(analyticsRepository.findByEventType("USER_LOGIN")).thenReturn(Arrays.asList(testAnalytics));

        List<AnalyticsDTO> result = analyticsService.getEventsByType("USER_LOGIN");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER_LOGIN", result.get(0).getEventType());

        verify(analyticsRepository, times(1)).findByEventType("USER_LOGIN");
    }
}
