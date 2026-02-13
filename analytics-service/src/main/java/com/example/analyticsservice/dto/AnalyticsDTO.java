package com.example.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {
    private Long id;
    private String eventType;
    private String resourceType;
    private Long resourceId;
    private String details;
    private LocalDateTime createdAt;
}
