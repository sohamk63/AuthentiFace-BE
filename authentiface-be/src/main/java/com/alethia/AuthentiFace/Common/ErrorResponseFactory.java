package com.alethia.AuthentiFace.Common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * DESIGN PATTERN: Factory
 * 
 * Centralizes the creation of ErrorResponse + ResponseEntity objects.
 * 
 * Before (repeated in every exception handler):
 *     ErrorResponse err = new ErrorResponse();
 *     err.setStatus(HttpStatus.NOT_FOUND.value());
 *     err.setError("Not Found");
 *     err.setMessage(ex.getMessage());
 *     err.setTimestamp(LocalDateTime.now());
 *     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
 * 
 * After (one-liner):
 *     return ErrorResponseFactory.create(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
 * 
 * Why Factory here?
 * - Eliminates duplicated creation logic across GlobalExceptionHandler and FaceExceptionHandler
 * - Single place to change if ErrorResponse structure evolves
 * - Uses the Builder pattern internally (patterns composing together)
 */
public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
        // Utility class — prevent instantiation
    }

    /**
     * Create a ResponseEntity containing a fully built ErrorResponse.
     *
     * @param status HTTP status (e.g., HttpStatus.NOT_FOUND)
     * @param error  Short error label (e.g., "User Not Found")
     * @param message Detailed message from the exception
     * @return ResponseEntity with the ErrorResponse body and matching status code
     */
    public static ResponseEntity<ErrorResponse> create(HttpStatus status, String error, String message) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
