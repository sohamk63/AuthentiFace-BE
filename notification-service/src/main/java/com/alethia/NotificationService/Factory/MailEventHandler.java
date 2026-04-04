package com.alethia.NotificationService.Factory;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.events.EventType;
import com.alethia.NotificationService.Entity.Notification;
import com.alethia.NotificationService.Entity.NotificationType;

@Component
public class MailEventHandler implements EventNotificationHandler {

    private static final Set<EventType> SUPPORTED = Set.of(
            EventType.MAIL_SENT,
            EventType.MAIL_RECEIVED,
            EventType.MAIL_DELIVERY_FAILED,
            EventType.CONFIDENTIAL_MAIL_RECEIVED
    );

    @Override
    public boolean canHandle(AuthentiFaceEvent event) {
        return SUPPORTED.contains(event.getEventType());
    }

    @Override
    public Notification handle(AuthentiFaceEvent event) {
        Map<String, Object> meta = event.getMetadata();
        String senderEmail = meta != null ? String.valueOf(meta.getOrDefault("senderEmail", "unknown")) : "unknown";

        return switch (event.getEventType()) {
            case MAIL_SENT -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Mail Sent")
                    .message("Your mail was sent successfully to " + meta.getOrDefault("recipientCount", "0") + " recipient(s)")
                    .type(NotificationType.INFO)
                    .sourceService(event.getSourceService())
                    .build();

            case MAIL_RECEIVED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("New Mail Received")
                    .message("You received a new mail from " + senderEmail)
                    .type(NotificationType.INFO)
                    .sourceService(event.getSourceService())
                    .build();

            case MAIL_DELIVERY_FAILED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Mail Delivery Failed")
                    .message("Failed to deliver your mail: " + meta.getOrDefault("reason", "Unknown error"))
                    .type(NotificationType.ERROR)
                    .sourceService(event.getSourceService())
                    .build();

            case CONFIDENTIAL_MAIL_RECEIVED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Confidential Mail Received")
                    .message("You received a confidential mail from " + senderEmail + ". Face verification required to open.")
                    .type(NotificationType.WARNING)
                    .sourceService(event.getSourceService())
                    .build();

            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getEventType());
        };
    }
}
