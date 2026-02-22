package com.alethia.AuthentiFace.MailService.Exception;

import java.util.UUID;

public class MailNotFoundException extends MailException {

    public MailNotFoundException(UUID mailId) {
        super("Mail not found with ID: " + mailId);
    }

    public MailNotFoundException(String message) {
        super(message);
    }
}
