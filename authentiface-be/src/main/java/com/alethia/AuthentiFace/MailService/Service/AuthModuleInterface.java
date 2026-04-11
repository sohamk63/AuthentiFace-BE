package com.alethia.AuthentiFace.MailService.Service;

import java.util.List;
import java.util.Map;
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

    /**
     * BATCH: Get user IDs for multiple emails in a single query.
     *
     * @param emails List of user emails
     * @return Map of email → userId for all found users
     */
    Map<String, UUID> getUserIdsByEmails(List<String> emails);
}
