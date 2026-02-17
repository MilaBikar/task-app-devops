package com.example.taskservice.messaging;

import com.example.taskservice.config.RabbitMQConfig;
import com.example.taskservice.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTaskEvent(TaskEvent event) {
        log.info("Publishing event: {} for taskId: {}", event.getEventType(), event.getTaskId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_EXCHANGE, "", event);
    }
}