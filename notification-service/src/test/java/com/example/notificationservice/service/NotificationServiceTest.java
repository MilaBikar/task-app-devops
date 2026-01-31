package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UNIT TESTS - NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationDTO testNotificationDTO;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotificationDTO = new NotificationDTO();
        testNotificationDTO.setMessage("Test notification message");
        testNotificationDTO.setRecipientUserId(1L);
        testNotificationDTO.setType(Notification.NotificationType.TASK_CREATED);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setMessage("Test notification message");
        testNotification.setRecipientUserId(1L);
        testNotification.setType(Notification.NotificationType.TASK_CREATED);
    }

    @Test
    @DisplayName("Should send notification successfully")
    void shouldSendNotificationSuccessfully() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        NotificationDTO result = notificationService.sendNotification(testNotificationDTO);

        assertNotNull(result);
        assertEquals("Test notification message", result.getMessage());
        assertEquals(1L, result.getRecipientUserId());
        assertEquals(Notification.NotificationType.TASK_CREATED, result.getType());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should get notification by ID successfully")
    void shouldGetNotificationByIdSuccessfully() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        NotificationDTO result = notificationService.getNotificationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test notification message", result.getMessage());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when notification not found by ID")
    void shouldThrowExceptionWhenNotificationNotFoundById() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        NotificationService.ResourceNotFoundException exception = assertThrows(
                NotificationService.ResourceNotFoundException.class,
                () -> notificationService.getNotificationById(999L)
        );

        assertEquals("Notification not found with id: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should get all notifications successfully")
    void shouldGetAllNotificationsSuccessfully() {
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setMessage("Second notification");
        notification2.setRecipientUserId(2L);
        notification2.setType(Notification.NotificationType.TASK_COMPLETED);

        when(notificationRepository.findAll()).thenReturn(Arrays.asList(testNotification, notification2));

        List<NotificationDTO> result = notificationService.getAllNotifications();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test notification message", result.get(0).getMessage());
        assertEquals("Second notification", result.get(1).getMessage());
        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get notifications by user ID successfully")
    void shouldGetNotificationsByUserIdSuccessfully() {
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setMessage("Another notification");
        notification2.setRecipientUserId(1L);
        notification2.setType(Notification.NotificationType.TASK_UPDATED);

        when(notificationRepository.findByRecipientUserIdOrderBySentAtDesc(1L))
                .thenReturn(Arrays.asList(testNotification, notification2));

        List<NotificationDTO> result = notificationService.getNotificationsByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getRecipientUserId());
        assertEquals(1L, result.get(1).getRecipientUserId());
        verify(notificationRepository, times(1)).findByRecipientUserIdOrderBySentAtDesc(1L);
    }

    @Test
    @DisplayName("Should delete notification successfully")
    void shouldDeleteNotificationSuccessfully() {
        when(notificationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(1L);

        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).existsById(1L);
        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent notification")
    void shouldThrowExceptionWhenDeletingNonExistentNotification() {
        when(notificationRepository.existsById(999L)).thenReturn(false);

        NotificationService.ResourceNotFoundException exception = assertThrows(
                NotificationService.ResourceNotFoundException.class,
                () -> notificationService.deleteNotification(999L)
        );

        assertEquals("Notification not found with id: 999", exception.getMessage());
        verify(notificationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle different notification types")
    void shouldHandleDifferentNotificationTypes() {
        NotificationDTO taskCompletedDTO = new NotificationDTO();
        taskCompletedDTO.setMessage("Task completed");
        taskCompletedDTO.setRecipientUserId(1L);
        taskCompletedDTO.setType(Notification.NotificationType.TASK_COMPLETED);

        Notification savedNotification = new Notification();
        savedNotification.setId(2L);
        savedNotification.setMessage("Task completed");
        savedNotification.setRecipientUserId(1L);
        savedNotification.setType(Notification.NotificationType.TASK_COMPLETED);

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        NotificationDTO result = notificationService.sendNotification(taskCompletedDTO);

        assertNotNull(result);
        assertEquals(Notification.NotificationType.TASK_COMPLETED, result.getType());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle long message")
    void shouldHandleLongMessage() {
        String longMessage = "A".repeat(500);
        testNotificationDTO.setMessage(longMessage);
        testNotification.setMessage(longMessage);

        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        NotificationDTO result = notificationService.sendNotification(testNotificationDTO);

        assertNotNull(result);
        assertEquals(500, result.getMessage().length());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should return empty list when user has no notifications")
    void shouldReturnEmptyListWhenUserHasNoNotifications() {
        when(notificationRepository.findByRecipientUserIdOrderBySentAtDesc(999L))
                .thenReturn(Arrays.asList());

        List<NotificationDTO> result = notificationService.getNotificationsByUserId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByRecipientUserIdOrderBySentAtDesc(999L);
    }
}