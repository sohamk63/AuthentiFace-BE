package com.alethia.AuthentiFace.MailService.Exception;

public class UserNotFoundException extends MailException {

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(String message, boolean isId) {
        super(isId ? "User not found with ID: " + message : "User not found: " + message);
    }
}
