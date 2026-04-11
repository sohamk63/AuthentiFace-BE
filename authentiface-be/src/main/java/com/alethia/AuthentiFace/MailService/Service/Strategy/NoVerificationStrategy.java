package com.alethia.AuthentiFace.MailService.Service.Strategy;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * STRATEGY: No verification required.
 * Used for non-confidential mails — always returns true.
 */
@Component
public class NoVerificationStrategy implements MailVerificationStrategy {

    @Override
    public boolean verify(UUID userId, List<MultipartFile> faceFrames) {
        // Non-confidential mail: no verification needed
        return true;
    }
}
