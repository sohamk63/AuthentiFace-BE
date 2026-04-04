package com.alethia.AuthentiFace.MailService.Exception;

public class InvalidMailException extends MailException {

    public InvalidMailException(String message) {
        super(message);
    }

    public InvalidMailException(String fieldName, String reason) {
        super("Invalid " + fieldName + ": " + reason);
    }
}
