package com.alethia.AuthentiFace.FaceVerificationService.Service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.alethia.AuthentiFace.Common.Event.DomainEventPublisher;
import com.alethia.AuthentiFace.Common.Event.FaceEnrolledDomainEvent;
import com.alethia.AuthentiFace.Common.Event.FaceEnrollFailedDomainEvent;
import com.alethia.AuthentiFace.Common.Event.FaceVerificationFailedDomainEvent;
import com.alethia.AuthentiFace.config.CacheNames;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
public class FaceServiceImpl implements FaceService {

    private static final Logger log = LoggerFactory.getLogger(FaceServiceImpl.class);

    private final FaceProfileRepository faceProfileRepository;
    private final FaceImageStorageService faceImageStorageService;
    private final FaceVerificationClient faceVerificationClient;
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final CachedEmbeddingProvider cachedEmbeddingProvider;
    private final Executor faceExecutor;

    public FaceServiceImpl(FaceProfileRepository faceProfileRepository,
            FaceImageStorageService faceImageStorageService,
            FaceVerificationClient faceVerificationClient,
            UserRepository userRepository,
            DomainEventPublisher domainEventPublisher,
            CachedEmbeddingProvider cachedEmbeddingProvider,
            @Qualifier("faceExecutor") Executor faceExecutor) {
        this.faceProfileRepository = faceProfileRepository;
        this.faceImageStorageService = faceImageStorageService;
        this.faceVerificationClient = faceVerificationClient;
        this.userRepository = userRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.cachedEmbeddingProvider = cachedEmbeddingProvider;
        this.faceExecutor = faceExecutor;
    }

    /**
     * MULTITHREADING: Embedding generation (HTTP call to Python) and image storage (disk I/O)
     * are independent operations. We run them in PARALLEL using CompletableFuture on the
     * "faceExecutor" thread pool.
     * 
     * BEFORE (sequential):
     *   embedding = generateEmbedding(frames);  // ~500ms (network I/O)
     *   faceKey = store(frame);                  // ~50ms  (disk I/O)
     *   TOTAL: ~550ms
     * 
     * AFTER (parallel):
     *   embeddingFuture = CompletableFuture.supplyAsync(() -> generateEmbedding(frames));
     *   faceKeyFuture   = CompletableFuture.supplyAsync(() -> store(frame));
     *   CompletableFuture.allOf(both).join();    // wait for both
     *   TOTAL: ~500ms (max of the two, not sum)
     */
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
            // PARALLEL: Run embedding generation and image storage concurrently
            // These are independent I/O operations — no reason to wait sequentially
            CompletableFuture<String> embeddingFuture = CompletableFuture.supplyAsync(
                    () -> faceVerificationClient.generateEmbedding(frames), faceExecutor);

            MultipartFile representativeFrame = frames.get(0);
            CompletableFuture<String> faceKeyFuture = CompletableFuture.supplyAsync(
                    () -> faceImageStorageService.store(representativeFrame), faceExecutor);

            // Wait for both to complete
            CompletableFuture.allOf(embeddingFuture, faceKeyFuture).join();

            String embedding = embeddingFuture.join();
            String faceKey = faceKeyFuture.join();

            log.info("Parallel enrollment complete: embedding and image stored for user {}", userId);

            // 3. Deactivate existing active profile
            Optional<FaceProfile> existingProfile =
                    faceProfileRepository.findByUserIdAndIsActiveTrue(userId);

            if (existingProfile.isPresent()) {
                FaceProfile profile = existingProfile.get();
                profile.setIsActive(false);
                faceProfileRepository.save(profile);

                // Delete old image asynchronously — non-critical, fire-and-forget
                String oldFaceKey = profile.getFaceKey();
                CompletableFuture.runAsync(() -> {
                    try {
                        faceImageStorageService.delete(oldFaceKey);
                    } catch (Exception e) {
                        log.warn("Failed to delete old face image: {}", e.getMessage());
                    }
                }, faceExecutor);
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

            // Observer pattern: publish domain event (will be handled async by eventExecutor)
            domainEventPublisher.publish(new FaceEnrolledDomainEvent(userId));

        } catch (Exception ex) {
            domainEventPublisher.publish(new FaceEnrollFailedDomainEvent(userId, ex.getMessage()));
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyFace(UUID userId, List<MultipartFile> frames) {
        String storedEmbedding = cachedEmbeddingProvider.getStoredEmbedding(userId);
        boolean result = faceVerificationClient.verify(storedEmbedding, frames);

        if (!result) {
            domainEventPublisher.publish(
                    new FaceVerificationFailedDomainEvent(userId, "Face verification mismatch"));
        }

        return result;
    }
}
