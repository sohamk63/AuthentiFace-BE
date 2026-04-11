package com.alethia.AuthentiFace.Kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alethia.AuthentiFace.Common.Event.FaceEnrollFailedDomainEvent;
import com.alethia.AuthentiFace.Common.Event.FaceEnrolledDomainEvent;
import com.alethia.AuthentiFace.Common.Event.FaceVerificationFailedDomainEvent;
import com.alethia.AuthentiFace.Common.Event.LoginFailedDomainEvent;
import com.alethia.AuthentiFace.Common.Event.LoginSuccessDomainEvent;
import com.alethia.AuthentiFace.Common.Event.MailSentDomainEvent;
import com.alethia.AuthentiFace.Kafka.producer.KafkaEventPublisher;

/**
 * ASYNC EVENT LISTENERS
 * 
 * Each @EventListener is also @Async("eventExecutor"), meaning:
 * - The calling service (e.g., MailServiceImpl.sendMail()) does NOT wait for Kafka
 * - Event forwarding runs on the "eventExecutor" thread pool (see AsyncConfig)
 * - The HTTP response returns immediately to the client
 * - If Kafka is slow or down, the main request is unaffected
 * 
 * BEFORE: sendMail() → publish domain event → [BLOCKS] → Kafka publish → return response
 * AFTER:  sendMail() → publish domain event → return response immediately
 *                                           └→ [eventExecutor thread] → Kafka publish
 */
@Component
public class KafkaDomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventListener.class);

    private final KafkaEventPublisher kafkaEventPublisher;

    public KafkaDomainEventListener(KafkaEventPublisher kafkaEventPublisher) {
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    // ── Auth Events ──

    @Async("eventExecutor")
    @EventListener
    public void onLoginSuccess(LoginSuccessDomainEvent event) {
        log.info("[async:event-thread] Login success for user {}", event.getUserId());
        kafkaEventPublisher.publishLoginSuccess(event.getUserId(), event.getEmail());
    }

    @Async("eventExecutor")
    @EventListener
    public void onLoginFailed(LoginFailedDomainEvent event) {
        log.info("[async:event-thread] Login failed for email {}", event.getEmail());
        kafkaEventPublisher.publishLoginFailed(event.getEmail(), event.getReason());
    }

    // ── Mail Events ──

    @Async("eventExecutor")
    @EventListener
    public void onMailSent(MailSentDomainEvent event) {
        log.info("[async:event-thread] Mail sent by user {}, mailId={}", event.getUserId(), event.getMailId());

        // Publish mail-sent event
        kafkaEventPublisher.publishMailSent(
                event.getUserId(),
                event.getSenderEmail(),
                event.getMailId(),
                event.getRecipientIds()
        );

        // Publish mail-received event for each recipient
        for (var recipientId : event.getRecipientIds()) {
            kafkaEventPublisher.publishMailReceived(
                    recipientId,
                    event.getMailId(),
                    event.getSenderEmail(),
                    event.isConfidential()
            );
        }
    }

    // ── Face Events ──

    @Async("eventExecutor")
    @EventListener
    public void onFaceEnrolled(FaceEnrolledDomainEvent event) {
        log.info("[async:event-thread] Face enrolled for user {}", event.getUserId());
        kafkaEventPublisher.publishFaceEnrollSuccess(event.getUserId());
    }

    @Async("eventExecutor")
    @EventListener
    public void onFaceEnrollFailed(FaceEnrollFailedDomainEvent event) {
        log.info("[async:event-thread] Face enrollment failed for user {}", event.getUserId());
        kafkaEventPublisher.publishFaceEnrollFailed(event.getUserId(), event.getReason());
    }

    @Async("eventExecutor")
    @EventListener
    public void onFaceVerificationFailed(FaceVerificationFailedDomainEvent event) {
        log.info("[async:event-thread] Face verification failed for user {}", event.getUserId());
        kafkaEventPublisher.publishFaceVerificationFailed(event.getUserId(), event.getContext());
    }
}
