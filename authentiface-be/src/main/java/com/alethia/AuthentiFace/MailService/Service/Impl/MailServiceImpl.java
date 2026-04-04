package com.alethia.AuthentiFace.MailService.Service.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.MailService.DTO.MailResponse;
import com.alethia.AuthentiFace.MailService.DTO.PageResponse;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest.RecipientRequest;
import com.alethia.AuthentiFace.MailService.DTO.SendMailResponse;
import com.alethia.AuthentiFace.MailService.DTO.SentMailResponse;
import com.alethia.AuthentiFace.MailService.Entity.Mail;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient.RecipientType;
import com.alethia.AuthentiFace.MailService.Exception.InvalidMailException;
import com.alethia.AuthentiFace.MailService.Exception.MailNotFoundException;
import com.alethia.AuthentiFace.MailService.Exception.UnauthorizedException;
import com.alethia.AuthentiFace.MailService.Exception.UserNotFoundException;
import com.alethia.AuthentiFace.MailService.Repository.MailRecipientRepository;
import com.alethia.AuthentiFace.MailService.Repository.MailRepository;
import com.alethia.AuthentiFace.MailService.Service.AuthModuleInterface;
import com.alethia.AuthentiFace.MailService.Service.MailService;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceService;
import com.alethia.AuthentiFace.Kafka.producer.KafkaEventPublisher;

@Service
@Transactional
public class MailServiceImpl implements MailService {

    private final MailRepository mailRepository;
    private final MailRecipientRepository mailRecipientRepository;
    private final AuthModuleInterface authModuleInterface;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final FaceService faceService;

    @Autowired
    public MailServiceImpl(
            MailRepository mailRepository,
            MailRecipientRepository mailRecipientRepository,
            AuthModuleInterface authModuleInterface,
            KafkaEventPublisher kafkaEventPublisher,
            FaceService faceService
    ) {
        this.mailRepository = mailRepository;
        this.mailRecipientRepository = mailRecipientRepository;
        this.authModuleInterface = authModuleInterface;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.faceService = faceService;
    }

    @Override
    public SendMailResponse sendMail(UUID senderId, SendMailRequest request) {
        // Validate sender exists
        if (!authModuleInterface.userExistsById(senderId)) {
            throw new UserNotFoundException("Sender user not found", true);
        }

        // Perform face verification for sending mail
        boolean isVerified = faceService.verifyFace(senderId, request.getFaceFrames());
        if (!isVerified) {
            throw new InvalidMailException("Face verification failed. Cannot send mail.");
        }

        // Validate mail content
        validateMailRequest(request);

        // Create mail entity
        Mail mail = new Mail();
        mail.setSubject(request.getSubject());
        mail.setBody(request.getBody());
        mail.setSenderId(senderId);
        mail.setIsConfidential(request.getIsConfidential() != null ? request.getIsConfidential() : false);

        // Save mail first
        Mail savedMail = mailRepository.save(mail);

        // Create and save mail recipients
        List<UUID> recipientIds = new ArrayList<>();
        for (RecipientRequest recipientReq : request.getRecipients()) {
            // Validate recipient exists
            UUID recipientId = authModuleInterface.getUserIdByEmail(recipientReq.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(recipientReq.getEmail()));

            // // Prevent self-sending
            // if (recipientId.equals(senderId)) {
            //     throw new InvalidMailException("Cannot send mail to yourself");
            // }

            // Create mail recipient entry
            MailRecipient recipient = new MailRecipient();
            recipient.setMail(savedMail);
            recipient.setRecipientId(recipientId);
            recipient.setType(RecipientType.valueOf(recipientReq.getType().toUpperCase()));
            recipient.setIsRead(false);
            recipient.setIsDeleted(false);

            mailRecipientRepository.save(recipient);
            recipientIds.add(recipientId);
        }

        // Publish Kafka event for mail sent
        String senderEmail = authModuleInterface.getUserEmailById(senderId).orElse("unknown");
        kafkaEventPublisher.publishMailSent(
                senderId,
                senderEmail,
                savedMail.getId(),
                recipientIds
        );

        // Also publish events for each recipient (mail received)
        for (UUID recipientId : recipientIds) {
            kafkaEventPublisher.publishMailReceived(
                    recipientId,
                    savedMail.getId(),
                    senderEmail,
                    savedMail.getIsConfidential() != null && savedMail.getIsConfidential()
            );
        }

        // Return response
        SendMailResponse response = new SendMailResponse();
        response.setMessage("Mail sent successfully");
        response.setMailId(savedMail.getId().toString());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MailResponse> getInbox(UUID recipientId, Pageable pageable) {
        Page<MailRecipient> mailRecipients = mailRecipientRepository.findInboxByRecipientId(
                recipientId,
                pageable
        );

        List<MailResponse> responses = mailRecipients.getContent().stream()
                .map(this::toMailResponse)
                .collect(Collectors.toList());

        Page<MailResponse> responsePage = mailRecipients.map(mr -> toMailResponse(mr));
        return new PageResponse<>(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SentMailResponse> getSentMails(UUID senderId, Pageable pageable) {
        Page<Mail> mails = mailRepository.findBySenderIdAndIsDeletedFalseOrderByCreatedAtDesc(
                senderId,
                pageable
        );

        Page<SentMailResponse> responses = mails.map(mail -> {
            SentMailResponse response = new SentMailResponse();
            response.setId(mail.getId());
            response.setSubject(mail.getSubject());
            response.setBody(mail.getBody());
            response.setCreatedAt(mail.getCreatedAt());
            response.setUpdatedAt(mail.getUpdatedAt());
            response.setIsConfidential(mail.getIsConfidential());

            // Count recipients
            List<MailRecipient> recipients = mailRecipientRepository.findByMailIdAndIsDeletedFalse(mail.getId());
            response.setRecipientCount(recipients.size());

            return response;
        });

        return new PageResponse<>(responses);
    }

    @Override
    public void markAsRead(UUID mailId, UUID recipientId) {
        MailRecipient mailRecipient = mailRecipientRepository.findByIdAndRecipientIdAndNotDeleted(
                mailId,
                recipientId
        ).orElseThrow(() -> new MailNotFoundException(
                "Mail not found or you are not a recipient of this mail"
        ));

        mailRecipient.setIsRead(true);
        mailRecipient.setReadAt(LocalDateTime.now());
        mailRecipientRepository.save(mailRecipient);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return mailRecipientRepository.countUnreadByRecipient(recipientId);
    }

    /**
     * Validate mail request content
     */
    private void validateMailRequest(SendMailRequest request) {
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new InvalidMailException("Subject cannot be empty");
        }

        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new InvalidMailException("Body cannot be empty");
        }

        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            throw new InvalidMailException("At least one recipient is required");
        }

        // Validate recipient type
        for (RecipientRequest recipient : request.getRecipients()) {
            try {
                RecipientType.valueOf(recipient.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidMailException(
                        "Recipient type must be one of: TO, CC, BCC"
                );
            }
        }
    }

    /**
     * Convert MailRecipient to MailResponse DTO
     */
    private MailResponse toMailResponse(MailRecipient mailRecipient) {
        Mail mail = mailRecipient.getMail();
        String senderEmail = authModuleInterface.getUserEmailById(mail.getSenderId())
                .orElse("Unknown Sender");

        MailResponse response = new MailResponse();
        response.setId(mail.getId());
        response.setSubject(mail.getSubject());
        response.setBody(mail.getBody());
        response.setSenderEmail(senderEmail);
        response.setCreatedAt(mail.getCreatedAt());
        response.setUpdatedAt(mail.getUpdatedAt());
        response.setIsRead(mailRecipient.getIsRead());
        response.setReadAt(mailRecipient.getReadAt());
        response.setIsConfidential(mail.getIsConfidential());

        return response;
    }

    @Override
    @Transactional
    public MailResponse openMail(UUID mailId, UUID recipientId, List<MultipartFile> faceFrames) {

        System.out.println("Opening mail with ID: " + mailId + " for recipient ID: " + recipientId);
        // Find the mail recipient entry
        MailRecipient mailRecipient = mailRecipientRepository.findByIdAndRecipientIdAndNotDeleted(
                mailId,
                recipientId
        ).orElseThrow(() -> new MailNotFoundException("Mail not found or access denied"));

        Mail mail = mailRecipient.getMail();

        // Check if confidential
        if (mail.getIsConfidential() != null && mail.getIsConfidential()) {
            System.out.println("Mail is confidential. Verifying face...");
            // Require face verification
            if (faceFrames == null || faceFrames.isEmpty()) {
                throw new InvalidMailException("Face verification required for confidential mail");
            }
            boolean isVerified = faceService.verifyFace(recipientId, faceFrames);
            if (!isVerified) {
                throw new InvalidMailException("Face verification failed. Cannot open confidential mail.");
            }
        }

        // Mark as read if not already
        if (!mailRecipient.getIsRead()) {
            mailRecipient.setIsRead(true);
            mailRecipient.setReadAt(LocalDateTime.now());
            mailRecipientRepository.save(mailRecipient);
        }

        // Return the mail response
        return toMailResponse(mailRecipient);
    }
}
