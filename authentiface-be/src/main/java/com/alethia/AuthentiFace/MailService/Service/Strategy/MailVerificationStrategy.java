package com.alethia.AuthentiFace.MailService.Service.Strategy;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * DESIGN PATTERN: Strategy
 * 
 * Defines a family of verification algorithms for mail access,
 * encapsulates each one, and makes them interchangeable.
 * 
 * The verification requirement changes based on whether a mail is confidential:
 *   - Non-confidential mail  → NoVerificationStrategy (always passes)
 *   - Confidential mail      → FaceVerificationStrategy (requires face match)
 * 
 * This eliminates if/else blocks in MailServiceImpl.openMail() and makes
 * it trivial to add new verification strategies in the future (e.g., OTP, PIN).
 */
public interface MailVerificationStrategy {

    /**
     * Verify that the user is authorized to access the mail.
     *
     * @param userId    UUID of the user requesting access
     * @param faceFrames Face image frames (may be null for non-face strategies)
     * @return true if verification passes
     * @throws RuntimeException if verification fails
     */
    boolean verify(UUID userId, List<MultipartFile> faceFrames);
}
