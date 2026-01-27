package com.example.notificationservice.service;

import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long id) {
        log.info("Fetching notification with id: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return convertToDTO(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        log.info("Fetching notifications for user: {}", userId);
        return notificationRepository.findByRecipientUserIdOrderBySentAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDTO sendNotification(NotificationDTO notificationDTO) {
        log.info("Sending notification to user {}: {}",
                notificationDTO.getRecipientUserId(),
                notificationDTO.getMessage());

        Notification notification = convertToEntity(notificationDTO);
        Notification savedNotification = notificationRepository.save(notification);

        // Simulacija slanja notifikacije (u stvarnosti bi ovde bio email/SMS/push notification)
        logNotification(savedNotification);

        log.info("Notification sent with id: {}", savedNotification.getId());
        return convertToDTO(savedNotification);
    }

    @Transactional
    public void deleteNotification(Long id) {
        log.info("Deleting notification with id: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
        log.info("Notification deleted: {}", id);
    }

    private void logNotification(Notification notification) {
        String divider = "=".repeat(60);
        log.info("\n{}\n📧 NOTIFICATION SENT\n{}", divider, divider);
        log.info("To User ID: {}", notification.getRecipientUserId());
        log.info("Type: {}", notification.getType());
        log.info("Message: {}", notification.getMessage());
        log.info("Sent At: {}", notification.getSentAt());
        log.info("{}\n", divider);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setRecipientUserId(notification.getRecipientUserId());
        dto.setType(notification.getType());
        dto.setSentAt(notification.getSentAt());
        return dto;
    }

    private Notification convertToEntity(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setMessage(dto.getMessage());
        notification.setRecipientUserId(dto.getRecipientUserId());
        notification.setType(dto.getType());
        return notification;
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}