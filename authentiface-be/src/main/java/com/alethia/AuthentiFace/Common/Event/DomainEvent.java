package com.alethia.AuthentiFace.Common.Event;

import java.util.Map;
import java.util.UUID;

/**
 * DESIGN PATTERN: Observer (Domain Event base class)
 * 
 * This is the "Subject notification" in the Observer pattern.
 * Services publish domain events via Spring's ApplicationEventPublisher,
 * and listeners (observers) react independently.
 * 
 * Why Observer here?
 * - Business logic (sending mail, enrolling face) should NOT know about Kafka.
 * - Services just announce "something happened" via a domain event.
 * - Listeners decide what to do: publish to Kafka, send email, update cache, etc.
 * - Adding new side effects (e.g., audit logging) requires ZERO changes to services.
 * 
 * This is the same pattern used by Spring's @EventListener, React's event system,
 * and every message broker (Kafka, RabbitMQ) at the infrastructure level.
 */
public abstract class DomainEvent {

    private final UUID userId;
    private final String source;
    private final Map<String, Object> metadata;

    protected DomainEvent(UUID userId, String source, Map<String, Object> metadata) {
        this.userId = userId;
        this.source = source;
        this.metadata = metadata;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSource() {
        return source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
