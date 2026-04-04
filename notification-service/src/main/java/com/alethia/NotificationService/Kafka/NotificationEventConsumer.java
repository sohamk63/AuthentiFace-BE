package com.alethia.NotificationService.Kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.events.KafkaTopics;
import com.alethia.NotificationService.Service.NotificationService;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.AUTH_EVENTS, groupId = "notification-service-group")
    public void consumeAuthEvent(AuthentiFaceEvent event) {
        log.info("Received auth event: {} for user {}", event.getEventType(), event.getUserId());
        notificationService.processEvent(event);
    }

    @KafkaListener(topics = KafkaTopics.MAIL_EVENTS, groupId = "notification-service-group")
    public void consumeMailEvent(AuthentiFaceEvent event) {
        log.info("Received mail event: {} for user {}", event.getEventType(), event.getUserId());
        notificationService.processEvent(event);
    }

    @KafkaListener(topics = KafkaTopics.FACE_EVENTS, groupId = "notification-service-group")
    public void consumeFaceEvent(AuthentiFaceEvent event) {
        log.info("Received face event: {} for user {}", event.getEventType(), event.getUserId());
        notificationService.processEvent(event);
    }
}
