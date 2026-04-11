package com.alethia.AuthentiFace.Common.Event;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event published on successful login.
 */
public class LoginSuccessDomainEvent extends DomainEvent {

    private final String email;

    public LoginSuccessDomainEvent(UUID userId, String email) {
        super(userId, "auth-service", Map.of("email", email));
        this.email = email;
    }

    public String getEmail() { return email; }
}
