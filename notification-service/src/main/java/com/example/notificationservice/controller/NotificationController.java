package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        log.info("GET /api/notifications - Fetching all notifications");
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        log.info("GET /api/notifications/{} - Fetching notification", id);
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUserId(@PathVariable Long userId) {
        log.info("GET /api/notifications/user/{} - Fetching notifications for user", userId);
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationDTO> sendNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        log.info("POST /api/notifications/send - Sending notification");
        NotificationDTO sentNotification = notificationService.sendNotification(notificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(sentNotification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("DELETE /api/notifications/{} - Deleting notification", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}