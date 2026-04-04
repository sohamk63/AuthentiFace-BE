package com.alethia.AuthentiFace.Kafka.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.events.EventType;
import com.alethia.events.KafkaTopics;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, AuthentiFaceEvent> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, AuthentiFaceEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ── Auth Events ──

    public void publishLoginSuccess(UUID userId, String email) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", email);
        publish(KafkaTopics.AUTH_EVENTS, EventType.USER_LOGIN_SUCCESS, userId, "auth-service", metadata);
    }

    public void publishLoginFailed(String email, String reason) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", email);
        metadata.put("reason", reason);
        // userId is null for failed logins since user may not exist
        publish(KafkaTopics.AUTH_EVENTS, EventType.USER_LOGIN_FAILED, null, "auth-service", metadata);
    }

    public void publishPasswordChanged(UUID userId, String email) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", email);
        publish(KafkaTopics.AUTH_EVENTS, EventType.PASSWORD_CHANGED, userId, "auth-service", metadata);
    }

    public void publishNewDeviceLogin(UUID userId, String email, String deviceInfo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", email);
        metadata.put("deviceInfo", deviceInfo);
        publish(KafkaTopics.AUTH_EVENTS, EventType.NEW_DEVICE_LOGIN, userId, "auth-service", metadata);
    }

    // ── Mail Events ──

    public void publishMailSent(UUID senderId, String senderEmail, UUID mailId, List<UUID> recipientIds) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mailId", mailId.toString());
        metadata.put("senderEmail", senderEmail);
        metadata.put("recipientCount", recipientIds.size());
        publish(KafkaTopics.MAIL_EVENTS, EventType.MAIL_SENT, senderId, "mail-service", metadata);
    }

    public void publishMailReceived(UUID recipientId, UUID mailId, String senderEmail, boolean isConfidential) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mailId", mailId.toString());
        metadata.put("senderEmail", senderEmail);
        metadata.put("isConfidential", isConfidential);

        EventType type = isConfidential ? EventType.CONFIDENTIAL_MAIL_RECEIVED : EventType.MAIL_RECEIVED;
        publish(KafkaTopics.MAIL_EVENTS, type, recipientId, "mail-service", metadata);
    }

    public void publishMailDeliveryFailed(UUID senderId, UUID mailId, String reason) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mailId", mailId.toString());
        metadata.put("reason", reason);
        publish(KafkaTopics.MAIL_EVENTS, EventType.MAIL_DELIVERY_FAILED, senderId, "mail-service", metadata);
    }

    // ── Face Events ──

    public void publishFaceEnrollSuccess(UUID userId) {
        Map<String, Object> metadata = new HashMap<>();
        publish(KafkaTopics.FACE_EVENTS, EventType.FACE_ENROLL_SUCCESS, userId, "face-service", metadata);
    }

    public void publishFaceEnrollFailed(UUID userId, String reason) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", reason);
        publish(KafkaTopics.FACE_EVENTS, EventType.FACE_ENROLL_FAILED, userId, "face-service", metadata);
    }

    public void publishFaceVerificationFailed(UUID userId, String context) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("context", context);
        publish(KafkaTopics.FACE_EVENTS, EventType.FACE_VERIFICATION_FAILED, userId, "face-service", metadata);
    }

    // ── Core publish method ──

    private void publish(String topic, EventType eventType, UUID userId, String source, Map<String, Object> metadata) {
        AuthentiFaceEvent event = AuthentiFaceEvent.create(eventType, userId, source, metadata);
        String key = userId != null ? userId.toString() : event.getEventId();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to topic {}: {}", eventType, topic, ex.getMessage());
                    } else {
                        log.info("Published event {} to topic {} [partition={}, offset={}]",
                                eventType, topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
