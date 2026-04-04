package com.alethia.AuthentiFace.MailService.Exception;

import java.util.UUID;

public class UnauthorizedException extends MailException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(UUID userId, UUID resourceId) {
        super("User " + userId + " is not authorized to access resource " + resourceId);
    }
}
