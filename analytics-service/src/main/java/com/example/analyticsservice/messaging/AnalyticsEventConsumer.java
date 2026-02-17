package com.example.analyticsservice.messaging;

import com.example.analyticsservice.config.RabbitMQConfig;
import com.example.analyticsservice.dto.AnalyticsDTO;
import com.example.analyticsservice.event.TaskEvent;
import com.example.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;

    @RabbitListener(queues = RabbitMQConfig.TASK_ANALYTICS_QUEUE)
    public void handleTaskEvent(TaskEvent event) {
        log.info("Analytics received: {} for taskId: {}", event.getEventType(), event.getTaskId());

        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setEventType(event.getEventType());
        dto.setResourceType("TASK");
        dto.setResourceId(event.getTaskId());
        dto.setDetails(
                "Task: \"" + event.getTitle()
                        + "\", userId: " + event.getUserId()
                        + (event.getStatus() != null ? ", status: " + event.getStatus() : "")
        );

        analyticsService.recordEvent(dto);
    }
}