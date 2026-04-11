package com.alethia.AuthentiFace.Common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alethia.AuthentiFace.MailService.Exception.InvalidMailException;
import com.alethia.AuthentiFace.MailService.Exception.MailException;
import com.alethia.AuthentiFace.MailService.Exception.MailNotFoundException;
import com.alethia.AuthentiFace.MailService.Exception.UnauthorizedException;
import com.alethia.AuthentiFace.MailService.Exception.UserNotFoundException;

/**
 * Uses FACTORY PATTERN (ErrorResponseFactory) + BUILDER PATTERN (ErrorResponse.builder())
 * to eliminate the repetitive error response construction that was in every handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return ErrorResponseFactory.create(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }

    @ExceptionHandler(MailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMailNotFoundException(MailNotFoundException ex) {
        return ErrorResponseFactory.create(HttpStatus.NOT_FOUND, "Mail Not Found", ex.getMessage());
    }

    @ExceptionHandler(InvalidMailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMailException(InvalidMailException ex) {
        return ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, "Invalid Mail", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return ErrorResponseFactory.create(HttpStatus.FORBIDDEN, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse> handleMailException(MailException ex) {
        return ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, "Mail Service Error", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException ex) {
        return ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }
}
