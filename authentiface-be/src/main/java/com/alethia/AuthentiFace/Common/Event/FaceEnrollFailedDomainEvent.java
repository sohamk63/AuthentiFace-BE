package com.alethia.AuthentiFace.Common.Event;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when face enrollment fails.
 */
public class FaceEnrollFailedDomainEvent extends DomainEvent {

    private final String reason;

    public FaceEnrollFailedDomainEvent(UUID userId, String reason) {
        super(userId, "face-service", Map.of("reason", reason));
        this.reason = reason;
    }

    public String getReason() { return reason; }
}
