package com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface FaceService {

    /**
     * Enroll a new face profile for a user
     * 
     * @param userId the user's UUID
     * @param frames list of face image frames for enrollment
     */
    void enrollFace(UUID userId, List<MultipartFile> frames);

    /**
     * Verify a user's face against their stored profile
     * 
     * @param userId the user's UUID
     * @param frames list of face image frames for verification
     * @return true if face is verified, false otherwise
     */
    boolean verifyFace(UUID userId, List<MultipartFile> frames);
}
