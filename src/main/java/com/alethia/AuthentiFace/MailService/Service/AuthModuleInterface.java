package com.alethia.AuthentiFace.MailService.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for inter-module communication with Auth module.
 * This ensures the Mail module does NOT directly access AuthRepository.
 * All user validation must go through this interface.
 */
public interface AuthModuleInterface {

    /**
     * Check if a user exists by email
     *
     * @param email User email
     * @return true if user exists, false otherwise
     */
    boolean userExistsByEmail(String email);

    /**
     * Check if a user exists by ID
     *
     * @param userId User UUID
     * @return true if user exists, false otherwise
     */
    boolean userExistsById(UUID userId);

    /**
     * Get user email by ID
     *
     * @param userId User UUID
     * @return Optional containing email if user exists, empty otherwise
     */
    Optional<String> getUserEmailById(UUID userId);

    /**
     * Get user UUID by email
     *
     * @param email User email
     * @return Optional containing UUID if user exists, empty otherwise
     */
    Optional<UUID> getUserIdByEmail(String email);
}
