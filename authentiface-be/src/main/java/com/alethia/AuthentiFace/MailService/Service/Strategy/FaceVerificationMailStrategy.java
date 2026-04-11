package com.alethia.AuthentiFace.MailService.Service.Strategy;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceService;
import com.alethia.AuthentiFace.MailService.Exception.InvalidMailException;

/**
 * STRATEGY: Face verification required.
 * Used for confidential mails — delegates to FaceService for biometric check.
 */
@Component
public class FaceVerificationMailStrategy implements MailVerificationStrategy {

    private final FaceService faceService;

    public FaceVerificationMailStrategy(FaceService faceService) {
        this.faceService = faceService;
    }

    @Override
    public boolean verify(UUID userId, List<MultipartFile> faceFrames) {
        if (faceFrames == null || faceFrames.isEmpty()) {
            throw new InvalidMailException("Face verification required for confidential mail");
        }

        boolean isVerified = faceService.verifyFace(userId, faceFrames);
        if (!isVerified) {
            throw new InvalidMailException("Face verification failed. Cannot access confidential mail.");
        }

        return true;
    }
}
