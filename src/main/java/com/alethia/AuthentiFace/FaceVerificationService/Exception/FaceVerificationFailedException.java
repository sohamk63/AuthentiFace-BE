package com.alethia.AuthentiFace.FaceVerificationService.Exception;

public class FaceVerificationFailedException extends FaceException {

    public FaceVerificationFailedException(String message) {
        super(message);
    }

    public FaceVerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
