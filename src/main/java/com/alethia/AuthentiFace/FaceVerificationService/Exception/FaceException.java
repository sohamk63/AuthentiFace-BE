package com.alethia.AuthentiFace.FaceVerificationService.Exception;

public class FaceException extends RuntimeException {

    public FaceException(String message) {
        super(message);
    }

    public FaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
