package com.example.notificationservice.dto;

import com.example.notificationservice.model.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 500, message = "Message must be between 1 and 500 characters")
    private String message;

    @NotNull(message = "Recipient user ID is required")
    private Long recipientUserId;

    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;

    private LocalDateTime sentAt;
}