package com.alethia.AuthentiFace.Common.Event;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when a face is successfully enrolled.
 */
public class FaceEnrolledDomainEvent extends DomainEvent {

    public FaceEnrolledDomainEvent(UUID userId) {
        super(userId, "face-service", Map.of());
    }
}
