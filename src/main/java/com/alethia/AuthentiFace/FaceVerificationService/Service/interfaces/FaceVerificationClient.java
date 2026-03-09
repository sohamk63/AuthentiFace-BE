package com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FaceVerificationClient {

    /**
     * Generate face embedding from an image
     * 
     * @param image the face image file
     * @return the face embedding as a string
     */
    String generateEmbedding(List<MultipartFile> images);

    /**
     * Verify face against stored embedding using multiple frames
     * 
     * @param storedEmbedding the stored embedding to verify against
     * @param frames list of face images to verify
     * @return true if face verification succeeds, false otherwise
     */
    boolean verify(String storedEmbedding, List<MultipartFile> frames);
}
