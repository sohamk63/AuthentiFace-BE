package com.alethia.NotificationService.Factory;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.events.EventType;
import com.alethia.NotificationService.Entity.Notification;
import com.alethia.NotificationService.Entity.NotificationType;

@Component
public class AuthEventHandler implements EventNotificationHandler {

    private static final Set<EventType> SUPPORTED = Set.of(
            EventType.USER_LOGIN_SUCCESS,
            EventType.USER_LOGIN_FAILED,
            EventType.PASSWORD_CHANGED,
            EventType.NEW_DEVICE_LOGIN
    );

    @Override
    public boolean canHandle(AuthentiFaceEvent event) {
        return SUPPORTED.contains(event.getEventType()) && event.getUserId() != null;
    }

    @Override
    public Notification handle(AuthentiFaceEvent event) {
        Map<String, Object> meta = event.getMetadata();
        String email = meta != null ? String.valueOf(meta.getOrDefault("email", "")) : "";

        return switch (event.getEventType()) {
            case USER_LOGIN_SUCCESS -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Login Successful")
                    .message("You logged in successfully as " + email)
                    .type(NotificationType.INFO)
                    .sourceService(event.getSourceService())
                    .build();

            case USER_LOGIN_FAILED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Login Failed")
                    .message("A failed login attempt was detected for " + email)
                    .type(NotificationType.SECURITY)
                    .sourceService(event.getSourceService())
                    .build();

            case PASSWORD_CHANGED -> Notification.builder()
                    .userId(event.getUserId())
                    .title("Password Changed")
                    .message("Your password was changed successfully")
                    .type(NotificationType.SECURITY)
                    .sourceService(event.getSourceService())
                    .build();

            case NEW_DEVICE_LOGIN -> Notification.builder()
                    .userId(event.getUserId())
                    .title("New Device Login")
                    .message("A login from a new device was detected for your account")
                    .type(NotificationType.SECURITY)
                    .sourceService(event.getSourceService())
                    .build();

            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getEventType());
        };
    }
}
