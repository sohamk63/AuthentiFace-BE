package com.alethia.AuthentiFace.MailService.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.MailService.DTO.MailResponse;
import com.alethia.AuthentiFace.MailService.DTO.PageResponse;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest;
import com.alethia.AuthentiFace.MailService.DTO.SendMailResponse;
import com.alethia.AuthentiFace.MailService.DTO.SentMailResponse;

/**
 * Service interface for mail operations.
 * Contains all business logic for mail management.
 */
public interface MailService {

    /**
     * Send a mail to one or multiple recipients
     *
     * @param senderId UUID of the authenticated user sending the mail
     * @param request Mail details and recipients
     * @return Response with mail ID and confirmation message
     * @throws UserNotFoundException if sender or any recipient doesn't exist
     * @throws InvalidMailException if mail details are invalid
     */
    SendMailResponse sendMail(UUID senderId, SendMailRequest request);

    /**
     * Get inbox for the authenticated user
     *
     * @param recipientId UUID of the authenticated user
     * @param pageable Pagination parameters
     * @return Paginated list of mails in inbox
     */
    PageResponse<MailResponse> getInbox(UUID recipientId, Pageable pageable);

    /**
     * Get sent mails for the authenticated user
     *
     * @param senderId UUID of the authenticated user
     * @param pageable Pagination parameters
     * @return Paginated list of sent mails
     */
    PageResponse<SentMailResponse> getSentMails(UUID senderId, Pageable pageable);

    /**
     * Mark a mail as read
     *
     * @param mailId UUID of the mail
     * @param recipientId UUID of the recipient marking mail as read
     * @throws MailNotFoundException if mail doesn't exist
     * @throws UnauthorizedException if recipient is not a recipient of this mail
     */
    void markAsRead(UUID mailId, UUID recipientId);

    /**
     * Get count of unread mails for a user
     *
     * @param recipientId UUID of the user
     * @return Count of unread mails
     */
    long getUnreadCount(UUID recipientId);

    /**
     * Open/read mail content, with face verification for confidential mails
     *
     * @param mailId UUID of the mail
     * @param recipientId UUID of the recipient
     * @param faceFrames List of face images for verification (required if confidential)
     * @return Mail content
     * @throws MailNotFoundException if mail doesn't exist
     * @throws UnauthorizedException if recipient is not a recipient of this mail
     * @throws FaceVerificationException if face verification fails for confidential mail
     */
    MailResponse openMail(UUID mailId, UUID recipientId, List<MultipartFile> faceFrames);
}
