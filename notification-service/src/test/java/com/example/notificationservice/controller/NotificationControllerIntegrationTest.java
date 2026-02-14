package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("INTEGRATION TESTS - NotificationController")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    @DisplayName("Should send notification via POST /api/notifications/send")
    void shouldSendNotificationViaAPI() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setMessage("Integration test notification");
        notificationDTO.setRecipientUserId(1L);
        notificationDTO.setType(Notification.NotificationType.TASK_CREATED);

        String notificationJson = objectMapper.writeValueAsString(notificationDTO);

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.message").value("Integration test notification"))
                .andExpect(jsonPath("$.recipientUserId").value(1))
                .andExpect(jsonPath("$.type").value("TASK_CREATED"))
                .andExpect(jsonPath("$.sentAt").exists());

        // Verify in database
        Notification savedNotification = notificationRepository.findAll().get(0);
        assertEquals("Integration test notification", savedNotification.getMessage());
        assertEquals(1L, savedNotification.getRecipientUserId());
    }

    @Test
    @DisplayName("Should return 400 when message is blank")
    void shouldReturn400WhenMessageBlank() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setMessage("");
        notificationDTO.setRecipientUserId(1L);
        notificationDTO.setType(Notification.NotificationType.TASK_CREATED);

        String notificationJson = objectMapper.writeValueAsString(notificationDTO);

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when recipientUserId is null")
    void shouldReturn400WhenRecipientUserIdNull() throws Exception {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setMessage("Test message");
        notificationDTO.setRecipientUserId(null);
        notificationDTO.setType(Notification.NotificationType.GENERAL);

        String notificationJson = objectMapper.writeValueAsString(notificationDTO);

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all notifications via GET /api/notifications")
    void shouldGetAllNotifications() throws Exception {
        Notification notification1 = new Notification();
        notification1.setMessage("Notification 1");
        notification1.setRecipientUserId(1L);
        notification1.setType(Notification.NotificationType.TASK_CREATED);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setMessage("Notification 2");
        notification2.setRecipientUserId(2L);
        notification2.setType(Notification.NotificationType.TASK_COMPLETED);
        notificationRepository.save(notification2);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message").value("Notification 1"))
                .andExpect(jsonPath("$[1].message").value("Notification 2"));
    }

    @Test
    @DisplayName("Should get notification by ID via GET /api/notifications/{id}")
    void shouldGetNotificationById() throws Exception {
        Notification notification = new Notification();
        notification.setMessage("Find me");
        notification.setRecipientUserId(1L);
        notification.setType(Notification.NotificationType.TASK_UPDATED);
        Notification savedNotification = notificationRepository.save(notification);

        mockMvc.perform(get("/api/notifications/" + savedNotification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedNotification.getId()))
                .andExpect(jsonPath("$.message").value("Find me"))
                .andExpect(jsonPath("$.recipientUserId").value(1));
    }

    @Test
    @DisplayName("Should return 404 when notification not found")
    void shouldReturn404WhenNotificationNotFound() throws Exception {
        mockMvc.perform(get("/api/notifications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Notification not found")));
    }

    @Test
    @DisplayName("Should get notifications by user ID via GET /api/notifications/user/{userId}")
    void shouldGetNotificationsByUserId() throws Exception {
        Notification notification1 = new Notification();
        notification1.setMessage("User 1 Notification 1");
        notification1.setRecipientUserId(1L);
        notification1.setType(Notification.NotificationType.TASK_CREATED);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setMessage("User 1 Notification 2");
        notification2.setRecipientUserId(1L);
        notification2.setType(Notification.NotificationType.TASK_COMPLETED);
        notificationRepository.save(notification2);

        Notification notification3 = new Notification();
        notification3.setMessage("User 2 Notification");
        notification3.setRecipientUserId(2L);
        notification3.setType(Notification.NotificationType.GENERAL);
        notificationRepository.save(notification3);

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].recipientUserId").value(1))
                .andExpect(jsonPath("$[1].recipientUserId").value(1));
    }

    @Test
    @DisplayName("Should delete notification via DELETE /api/notifications/{id}")
    void shouldDeleteNotification() throws Exception {
        Notification notification = new Notification();
        notification.setMessage("To delete");
        notification.setRecipientUserId(1L);
        notification.setType(Notification.NotificationType.TASK_DELETED);
        Notification savedNotification = notificationRepository.save(notification);

        mockMvc.perform(delete("/api/notifications/" + savedNotification.getId()))
                .andExpect(status().isNoContent());

        assertFalse(notificationRepository.existsById(savedNotification.getId()));
    }

    @Test
    @DisplayName("Should handle different notification types")
    void shouldHandleDifferentNotificationTypes() throws Exception {
        // Test TASK_CREATED
        NotificationDTO taskCreated = new NotificationDTO();
        taskCreated.setMessage("Task created notification");
        taskCreated.setRecipientUserId(1L);
        taskCreated.setType(Notification.NotificationType.TASK_CREATED);

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreated)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TASK_CREATED"));

        NotificationDTO taskCompleted = new NotificationDTO();
        taskCompleted.setMessage("Task completed notification");
        taskCompleted.setRecipientUserId(1L);
        taskCompleted.setType(Notification.NotificationType.TASK_COMPLETED);

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCompleted)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TASK_COMPLETED"));

        assertEquals(2, notificationRepository.count());
    }

    @Test
    @DisplayName("Should return empty list when user has no notifications")
    void shouldReturnEmptyListWhenUserHasNoNotifications() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle long notification message")
    void shouldHandleLongNotificationMessage() throws Exception {
        String longMessage = "This is a very long notification message. ".repeat(20); // ~800 chars
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setMessage(longMessage.substring(0, 500));
        notificationDTO.setRecipientUserId(1L);
        notificationDTO.setType(Notification.NotificationType.GENERAL);

        String notificationJson = objectMapper.writeValueAsString(notificationDTO);

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(hasLength(500)));
    }
}