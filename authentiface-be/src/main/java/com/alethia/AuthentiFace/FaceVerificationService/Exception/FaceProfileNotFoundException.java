package com.alethia.AuthentiFace.FaceVerificationService.Exception;

public class FaceProfileNotFoundException extends FaceException {

    public FaceProfileNotFoundException(String message) {
        super(message);
    }

    public FaceProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
