package com.alethia.NotificationService.Factory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alethia.events.AuthentiFaceEvent;

@Component
public class EventHandlerFactory {

    private static final Logger log = LoggerFactory.getLogger(EventHandlerFactory.class);

    private final List<EventNotificationHandler> handlers;

    public EventHandlerFactory(List<EventNotificationHandler> handlers) {
        this.handlers = handlers;
    }

    public EventNotificationHandler getHandler(AuthentiFaceEvent event) {
        return handlers.stream()
                .filter(h -> h.canHandle(event))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No handler found for event type: {}", event.getEventType());
                    return new IllegalArgumentException("No handler for event type: " + event.getEventType());
                });
    }
}
