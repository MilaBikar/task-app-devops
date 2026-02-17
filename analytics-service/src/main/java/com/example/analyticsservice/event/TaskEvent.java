package com.example.analyticsservice.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TaskEvent {
    private Long taskId;
    private String title;
    private Long userId;
    private String eventType;
    private String status;
    private LocalDateTime timestamp;
}