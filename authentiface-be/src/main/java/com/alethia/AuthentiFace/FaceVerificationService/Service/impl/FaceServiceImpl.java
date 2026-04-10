package com.alethia.AuthentiFace.FaceVerificationService.Service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;
import com.alethia.AuthentiFace.FaceVerificationService.Entity.FaceProfile;
import com.alethia.AuthentiFace.FaceVerificationService.Repository.FaceProfileRepository;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceImageStorageService;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceService;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceVerificationClient;
import com.alethia.AuthentiFace.Kafka.producer.KafkaEventPublisher;
import com.alethia.AuthentiFace.config.CacheNames;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
public class FaceServiceImpl implements FaceService {

    private final FaceProfileRepository faceProfileRepository;
    private final FaceImageStorageService faceImageStorageService;
    private final FaceVerificationClient faceVerificationClient;
    private final UserRepository userRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CachedEmbeddingProvider cachedEmbeddingProvider;

    public FaceServiceImpl(FaceProfileRepository faceProfileRepository,
            FaceImageStorageService faceImageStorageService,
            FaceVerificationClient faceVerificationClient,
            UserRepository userRepository,
            KafkaEventPublisher kafkaEventPublisher,
            CachedEmbeddingProvider cachedEmbeddingProvider) {
        this.faceProfileRepository = faceProfileRepository;
        this.faceImageStorageService = faceImageStorageService;
        this.faceVerificationClient = faceVerificationClient;
        this.userRepository = userRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.cachedEmbeddingProvider = cachedEmbeddingProvider;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.FACE_EMBEDDINGS, key = "#userId"),
            @CacheEvict(value = CacheNames.FACE_ENROLLMENT_STATUS, key = "#userId")
    })
    public void enrollFace(UUID userId, List<MultipartFile> frames) {

        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("Enrollment requires multiple frames.");
        }

        if (frames.size() < 3) {
            throw new IllegalArgumentException("Minimum 3 frames required for enrollment.");
        }

        try {
            // 1. Generate averaged embedding from ALL frames via Python service
            String embedding = faceVerificationClient.generateEmbedding(frames);

            // 2. Store one representative image (optional)
            MultipartFile representativeFrame = frames.get(0);
            String faceKey = faceImageStorageService.store(representativeFrame);

            // 3. Deactivate existing active profile
            Optional<FaceProfile> existingProfile =
                    faceProfileRepository.findByUserIdAndIsActiveTrue(userId);

            if (existingProfile.isPresent()) {
                FaceProfile profile = existingProfile.get();
                profile.setIsActive(false);
                faceProfileRepository.save(profile);

                try {
                    faceImageStorageService.delete(profile.getFaceKey());
                } catch (Exception e) {
                    System.err.println("Warning: Failed to delete old face image: " + e.getMessage());
                }
            }

            // 4. Create new profile
            FaceProfile newProfile = new FaceProfile();
            newProfile.setUserId(userId);
            newProfile.setFaceKey(faceKey);
            newProfile.setEmbedding(embedding);
            newProfile.setIsActive(true);

            faceProfileRepository.save(newProfile);

            // 5. Update user's faceEnrolled status to true
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setFaceEnrolled(true);
                userRepository.save(user);
            } else {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            // Publish face enrollment success event
            kafkaEventPublisher.publishFaceEnrollSuccess(userId);

        } catch (Exception ex) {
            // Publish face enrollment failure event
            kafkaEventPublisher.publishFaceEnrollFailed(userId, ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyFace(UUID userId, List<MultipartFile> frames) {
        String storedEmbedding = cachedEmbeddingProvider.getStoredEmbedding(userId);
        boolean result = faceVerificationClient.verify(storedEmbedding, frames);

        if (!result) {
            kafkaEventPublisher.publishFaceVerificationFailed(userId, "Face verification mismatch");
        }

        return result;
    }
}
