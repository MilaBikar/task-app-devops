package com.example.analyticsservice.controller;

import com.example.analyticsservice.dto.AnalyticsDTO;
import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.repository.AnalyticsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("INTEGRATION TESTS - AnalyticsController")
class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        analyticsRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create event via POST /api/analytics")
    void shouldCreateEventViaAPI() throws Exception {
        AnalyticsDTO analyticsDTO = new AnalyticsDTO(
                null,
                "USER_SIGNUP",
                "USER",
                100L,
                "New user signed up",
                null);

        String analyticsJson = objectMapper.writeValueAsString(analyticsDTO);

        mockMvc.perform(post("/api/analytics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(analyticsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventType").value("USER_SIGNUP"))
                .andExpect(jsonPath("$.createdAt").exists());

        assertEquals(1, analyticsRepository.count());
    }

    @Test
    @DisplayName("Should get all events via GET /api/analytics")
    void shouldGetAllEvents() throws Exception {
        Analytics event1 = new Analytics();
        event1.setEventType("LOGIN");
        event1.setResourceType("USER");
        event1.setResourceId(1L);
        analyticsRepository.save(event1);

        Analytics event2 = new Analytics();
        event2.setEventType("LOGOUT");
        event2.setResourceType("USER");
        event2.setResourceId(1L);
        analyticsRepository.save(event2);

        mockMvc.perform(get("/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventType").value("LOGIN"))
                .andExpect(jsonPath("$[1].eventType").value("LOGOUT"));
    }

    @Test
    @DisplayName("Should get events by type via GET /api/analytics/type/{eventType}")
    void shouldGetEventsByType() throws Exception {
        Analytics event1 = new Analytics();
        event1.setEventType("PURCHASE");
        event1.setResourceType("ORDER");
        event1.setResourceId(1L);
        analyticsRepository.save(event1);

        Analytics event2 = new Analytics();
        event2.setEventType("LOGIN");
        event2.setResourceType("USER");
        event2.setResourceId(1L);
        analyticsRepository.save(event2);

        mockMvc.perform(get("/api/analytics/type/PURCHASE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType").value("PURCHASE"));
    }
}
