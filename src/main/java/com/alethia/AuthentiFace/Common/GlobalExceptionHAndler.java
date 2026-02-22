package com.alethia.AuthentiFace.Common;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alethia.AuthentiFace.MailService.Exception.InvalidMailException;
import com.alethia.AuthentiFace.MailService.Exception.MailException;
import com.alethia.AuthentiFace.MailService.Exception.MailNotFoundException;
import com.alethia.AuthentiFace.MailService.Exception.UnauthorizedException;
import com.alethia.AuthentiFace.MailService.Exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(HttpStatus.NOT_FOUND.value());
        err.setError("User Not Found");
        err.setMessage(ex.getMessage());
        err.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(MailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMailNotFoundException(MailNotFoundException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(HttpStatus.NOT_FOUND.value());
        err.setError("Mail Not Found");
        err.setMessage(ex.getMessage());
        err.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(InvalidMailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMailException(InvalidMailException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError("Invalid Mail");
        err.setMessage(ex.getMessage());
        err.setTimestamp(LocalDateTime.now());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(HttpStatus.FORBIDDEN.value());
        err.setError("Unauthorized");
        err.setMessage(ex.getMessage());
        err.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse> handleMailException(MailException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError("Mail Service Error");
        err.setMessage(ex.getMessage());
        err.setTimestamp(LocalDateTime.now());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException ex) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError("Bad Request");
        err.setMessage(ex.getMessage());
        err.setTimestamp(LocalDateTime.now());
        return ResponseEntity.badRequest().body(err);
    }
}
