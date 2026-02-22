package com.alethia.AuthentiFace.MailService.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alethia.AuthentiFace.MailService.Entity.Mail;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient;

@Repository
public interface MailRecipientRepository extends JpaRepository<MailRecipient, UUID> {

    /**
     * Find all mail recipients for a specific mail
     */
    List<MailRecipient> findByMailIdAndIsDeletedFalse(UUID mailId);

    /**
     * Find inbox for a recipient with pagination
     */
    @Query("SELECT mr FROM MailRecipient mr " +
           "JOIN FETCH mr.mail m " +
           "WHERE mr.recipientId = :recipientId AND mr.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<MailRecipient> findInboxByRecipientId(
            @Param("recipientId") UUID recipientId,
            Pageable pageable
    );

    /**
     * Find a specific mail recipient for a recipient (security check)
     */
    @Query("SELECT mr FROM MailRecipient mr " +
           "JOIN FETCH mr.mail m " +
           "WHERE mr.id = :recipientId AND mr.recipientId = :userId AND mr.isDeleted = false")
    Optional<MailRecipient> findByIdAndRecipientIdAndNotDeleted(
            @Param("recipientId") UUID recipientId,
            @Param("userId") UUID userId
    );

    /**
     * Check if a user is a recipient of a mail
     */
    @Query("SELECT COUNT(mr) > 0 FROM MailRecipient mr " +
           "WHERE mr.mail.id = :mailId AND mr.recipientId = :recipientId AND mr.isDeleted = false")
    boolean isUserRecipientOfMail(
            @Param("mailId") UUID mailId,
            @Param("recipientId") UUID recipientId
    );

    /**
     * Find unread mails count for a recipient
     */
    @Query("SELECT COUNT(mr) FROM MailRecipient mr " +
           "WHERE mr.recipientId = :recipientId AND mr.isRead = false AND mr.isDeleted = false")
    long countUnreadByRecipient(
            @Param("recipientId") UUID recipientId
    );

    /**
     * Find all recipient IDs for a mail (for event publishing)
     */
    @Query("SELECT mr.recipientId FROM MailRecipient mr WHERE mr.mail.id = :mailId AND mr.isDeleted = false")
    List<UUID> findRecipientIdsByMailId(
            @Param("mailId") UUID mailId
    );
}
