package com.example.notificationservice.messaging;

import com.example.notificationservice.config.RabbitMQConfig;
import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.event.TaskEvent;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.TASK_NOTIFICATION_QUEUE)
    public void handleTaskEvent(TaskEvent event) {
        log.info("Received task event: {} for userId: {}", event.getEventType(), event.getUserId());

        String message = switch (event.getEventType()) {
            case "TASK_CREATED" -> "New task created: \"" + event.getTitle() + "\"";
            case "TASK_UPDATED" -> "Task updated: \"" + event.getTitle()
                    + "\" (new status: " + event.getStatus() + ")";
            case "TASK_DELETED" -> "Task (ID: " + event.getTaskId() + ") is deleted.";
            default             -> "Task event: " + event.getEventType();
        };

        Notification.NotificationType type = switch (event.getEventType()) {
            case "TASK_CREATED" -> Notification.NotificationType.TASK_CREATED;
            case "TASK_UPDATED" -> Notification.NotificationType.TASK_UPDATED;
            case "TASK_DELETED" -> Notification.NotificationType.TASK_DELETED;
            default             -> Notification.NotificationType.GENERAL;
        };

        NotificationDTO notification = new NotificationDTO();
        notification.setRecipientUserId(event.getUserId());
        notification.setMessage(message);
        notification.setType(type);

        notificationService.sendNotification(notification);
    }
}