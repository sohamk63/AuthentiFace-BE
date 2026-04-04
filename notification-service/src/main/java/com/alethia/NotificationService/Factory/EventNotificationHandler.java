package com.alethia.NotificationService.Factory;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.NotificationService.Entity.Notification;

public interface EventNotificationHandler {

    boolean canHandle(AuthentiFaceEvent event);

    Notification handle(AuthentiFaceEvent event);
}
