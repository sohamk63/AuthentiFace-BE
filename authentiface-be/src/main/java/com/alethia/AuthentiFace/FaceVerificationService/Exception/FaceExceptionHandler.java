package com.alethia.AuthentiFace.FaceVerificationService.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alethia.AuthentiFace.Common.ErrorResponse;
import com.alethia.AuthentiFace.Common.ErrorResponseFactory;

/**
 * Uses FACTORY PATTERN (ErrorResponseFactory) + BUILDER PATTERN (ErrorResponse.builder())
 * to eliminate the repetitive error response construction that was in every handler.
 */
@RestControllerAdvice
public class FaceExceptionHandler {

    @ExceptionHandler(FaceProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFaceProfileNotFoundException(
            FaceProfileNotFoundException ex) {
        return ErrorResponseFactory.create(HttpStatus.NOT_FOUND, "FaceProfileNotFound", ex.getMessage());
    }

    @ExceptionHandler(FaceVerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleFaceVerificationFailedException(
            FaceVerificationFailedException ex) {
        return ErrorResponseFactory.create(HttpStatus.BAD_REQUEST, "FaceVerificationFailed", ex.getMessage());
    }

    @ExceptionHandler(FaceException.class)
    public ResponseEntity<ErrorResponse> handleFaceException(FaceException ex) {
        return ErrorResponseFactory.create(HttpStatus.INTERNAL_SERVER_ERROR, "FaceServiceError", ex.getMessage());
    }
}
