package com.example.taskservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskEvent {

    private Long taskId;
    private String title;
    private Long userId;
    private String eventType;
    private String status;
    private LocalDateTime timestamp;

    public static TaskEvent created(Long taskId, String title, Long userId, String status) {
        return new TaskEvent(taskId, title, userId, "TASK_CREATED", status, LocalDateTime.now());
    }

    public static TaskEvent updated(Long taskId, String title, Long userId, String status) {
        return new TaskEvent(taskId, title, userId, "TASK_UPDATED", status, LocalDateTime.now());
    }

    public static TaskEvent deleted(Long taskId, Long userId) {
        return new TaskEvent(taskId, null, userId, "TASK_DELETED", null, LocalDateTime.now());
    }
}