package com.alethia.AuthentiFace.FaceVerificationService.Service.impl;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.alethia.AuthentiFace.FaceVerificationService.Entity.FaceProfile;
import com.alethia.AuthentiFace.FaceVerificationService.Exception.FaceProfileNotFoundException;
import com.alethia.AuthentiFace.FaceVerificationService.Repository.FaceProfileRepository;
import com.alethia.AuthentiFace.config.CacheNames;

/**
 * Dedicated component for cached face embedding lookups.
 * Separated from FaceServiceImpl so that Spring AOP proxying works correctly
 * with @Cacheable (self-invocation bypasses the proxy).
 */
@Component
public class CachedEmbeddingProvider {

    private final FaceProfileRepository faceProfileRepository;

    public CachedEmbeddingProvider(FaceProfileRepository faceProfileRepository) {
        this.faceProfileRepository = faceProfileRepository;
    }

    @Cacheable(value = CacheNames.FACE_EMBEDDINGS, key = "#userId")
    public String getStoredEmbedding(UUID userId) {
        return faceProfileRepository.findByUserIdAndIsActiveTrue(userId)
                .map(FaceProfile::getEmbedding)
                .orElseThrow(() -> new FaceProfileNotFoundException(
                        "No active face profile found for user: " + userId));
    }
}
