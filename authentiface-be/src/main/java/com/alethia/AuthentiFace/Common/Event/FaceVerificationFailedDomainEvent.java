package com.alethia.AuthentiFace.Common.Event;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when face verification fails.
 */
public class FaceVerificationFailedDomainEvent extends DomainEvent {

    private final String context;

    public FaceVerificationFailedDomainEvent(UUID userId, String context) {
        super(userId, "face-service", Map.of("context", context));
        this.context = context;
    }

    public String getContext() { return context; }
}
