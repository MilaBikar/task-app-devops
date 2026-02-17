package com.example.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_NOTIFICATION_QUEUE = "task.notification.queue";
    public static final String TASK_EXCHANGE = "task.events";

    @Bean
    public Queue taskNotificationQueue() {
        return new Queue(TASK_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public FanoutExchange taskExchange() {
        return new FanoutExchange(TASK_EXCHANGE);
    }

    @Bean
    public Binding taskNotificationBinding() {
        return BindingBuilder
                .bind(taskNotificationQueue())
                .to(taskExchange());
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}