package com.alethia.NotificationService.Service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.NotificationService.DTO.NotificationPageResponse;

public interface NotificationService {

    void processEvent(AuthentiFaceEvent event);

    NotificationPageResponse getNotifications(UUID userId, Pageable pageable);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    long getUnreadCount(UUID userId);
}
