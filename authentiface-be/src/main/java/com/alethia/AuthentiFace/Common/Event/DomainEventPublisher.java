package com.alethia.AuthentiFace.Common.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: Observer (Publisher/Subject side)
 * 
 * Thin wrapper around Spring's ApplicationEventPublisher.
 * Services call this to announce domain events without knowing
 * who (or how many) listeners will react.
 * 
 * This is the "Subject" in the Observer pattern — it publishes
 * notifications, and any number of @EventListener methods can observe them.
 */
@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Publish a domain event. All registered @EventListener methods
     * matching this event type will be invoked.
     */
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} for user: {}",
                event.getClass().getSimpleName(), event.getUserId());
        applicationEventPublisher.publishEvent(event);
    }
}
