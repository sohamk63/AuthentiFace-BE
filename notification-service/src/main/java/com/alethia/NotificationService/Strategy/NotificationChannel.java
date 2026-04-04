package com.alethia.NotificationService.Strategy;

import com.alethia.NotificationService.Entity.Notification;

public interface NotificationChannel {

    void send(Notification notification);

    boolean supports(Notification notification);

    String getChannelName();
}
