package com.alethia.NotificationService.Strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alethia.NotificationService.Entity.Notification;
import com.alethia.NotificationService.Repository.NotificationRepository;

@Component
public class InAppNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationChannel.class);

    private final NotificationRepository notificationRepository;

    public InAppNotificationChannel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void send(Notification notification) {
        notificationRepository.save(notification);
        log.info("In-app notification saved for user {}: {}", notification.getUserId(), notification.getTitle());
    }

    @Override
    public boolean supports(Notification notification) {
        return true;
    }

    @Override
    public String getChannelName() {
        return "IN_APP";
    }
}
