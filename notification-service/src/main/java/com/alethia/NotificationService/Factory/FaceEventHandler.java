package com.alethia.NotificationService.Factory;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.events.EventType;
import com.alethia.NotificationService.Entity.Notification;
import com.alethia.NotificationService.Entity.NotificationType;

@Component
public class FaceEventHandler implements EventNotificationHandler {

    private static final Set<EventType> SUPPORTED = Set.of(
            EventType.FACE_ENROLL_SUCCESS,
            EventType.FACE_ENROLL_FAILED,
            EventType.FACE_VERIFICATION_FAILED
    );

    @Override
    public boolean canHandle(AuthentiFaceEvent event) {
        return SUPPORTED.contains(event.getEventType());
    }

    @Override
    public Notification handle(AuthentiFaceEvent event) {
        Map<String, Object> meta = event.getMetadata();

        return switch (event.getEventType()) {
            case FACE_ENROLL_SUCCESS -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Face Enrolled Successfully")
                    .message("Your face profile has been enrolled successfully. You can now use face verification.")
                    .type(NotificationType.INFO)
                    .sourceService(event.getSourceService())
                    .build();

            case FACE_ENROLL_FAILED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Face Enrollment Failed")
                    .message("Face enrollment failed: " + (meta != null ? meta.getOrDefault("reason", "Unknown error") : "Unknown error"))
                    .type(NotificationType.ERROR)
                    .sourceService(event.getSourceService())
                    .build();

            case FACE_VERIFICATION_FAILED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Face Verification Failed")
                    .message("A face verification attempt failed on your account. If this wasn't you, please secure your account.")
                    .type(NotificationType.SECURITY)
                    .sourceService(event.getSourceService())
                    .build();

            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getEventType());
        };
    }
}
