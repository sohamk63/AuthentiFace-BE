package com.alethia.AuthentiFace.Common.Event;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event published on failed login.
 */
public class LoginFailedDomainEvent extends DomainEvent {

    private final String email;
    private final String reason;

    public LoginFailedDomainEvent(String email, String reason) {
        super(null, "auth-service", Map.of("email", email, "reason", reason));
        this.email = email;
        this.reason = reason;
    }

    public String getEmail() { return email; }
    public String getReason() { return reason; }
}
