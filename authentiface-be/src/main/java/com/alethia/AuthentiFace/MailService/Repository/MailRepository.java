package com.alethia.AuthentiFace.MailService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alethia.AuthentiFace.MailService.Entity.Mail;

@Repository
public interface MailRepository extends JpaRepository<Mail, UUID> {

    /**
     * Find all mails sent by a specific user with pagination
     */
    Page<Mail> findBySenderIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID senderId,
            Pageable pageable
    );

    /**
     * Find a specific mail by ID and sender ID (security check)
     */
    @Query("SELECT m FROM Mail m WHERE m.id = :mailId AND m.senderId = :senderId AND m.isDeleted = false")
    java.util.Optional<Mail> findByIdAndSenderIdAndNotDeleted(
            @Param("mailId") UUID mailId,
            @Param("senderId") UUID senderId
    );

    /**
     * Count unread mails for a recipient
     */
    @Query("SELECT COUNT(mr) FROM MailRecipient mr WHERE mr.recipientId = :recipientId AND mr.isRead = false AND mr.isDeleted = false")
    long countUnreadByRecipient(
            @Param("recipientId") UUID recipientId
    );
}
