package com.alethia.AuthentiFace.MailService.Service.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.MailService.DTO.AttachmentResponse;
import com.alethia.AuthentiFace.MailService.DTO.MailResponse;
import com.alethia.AuthentiFace.MailService.DTO.PageResponse;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest.RecipientRequest;
import com.alethia.AuthentiFace.MailService.DTO.SendMailResponse;
import com.alethia.AuthentiFace.MailService.DTO.SentMailResponse;
import com.alethia.AuthentiFace.MailService.Entity.Attachment;
import com.alethia.AuthentiFace.MailService.Entity.Mail;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient.RecipientType;
import com.alethia.AuthentiFace.MailService.Exception.InvalidMailException;
import com.alethia.AuthentiFace.MailService.Exception.MailNotFoundException;
import com.alethia.AuthentiFace.MailService.Exception.UnauthorizedException;
import com.alethia.AuthentiFace.MailService.Exception.UserNotFoundException;
import com.alethia.AuthentiFace.MailService.Repository.AttachmentRepository;
import com.alethia.AuthentiFace.MailService.Repository.MailRecipientRepository;
import com.alethia.AuthentiFace.MailService.Repository.MailRepository;
import com.alethia.AuthentiFace.MailService.Service.AuthModuleInterface;
import com.alethia.AuthentiFace.MailService.Service.MailService;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceService;
import com.alethia.AuthentiFace.Common.Event.DomainEventPublisher;
import com.alethia.AuthentiFace.Common.Event.MailSentDomainEvent;
import com.alethia.AuthentiFace.MailService.Service.Strategy.MailVerificationStrategy;
import com.alethia.AuthentiFace.MailService.Service.Strategy.MailVerificationStrategyResolver;
import com.alethia.AuthentiFace.config.CacheNames;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
@Transactional
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final MailRepository mailRepository;
    private final MailRecipientRepository mailRecipientRepository;
    private final AttachmentRepository attachmentRepository;
    private final AuthModuleInterface authModuleInterface;
    private final DomainEventPublisher domainEventPublisher;
    private final FaceService faceService;
    private final MailVerificationStrategyResolver strategyResolver;
    private final AsyncMailProcessor asyncMailProcessor;
    private final AttachmentValidator attachmentValidator;
    private final Executor eventExecutor;

    @Autowired
    public MailServiceImpl(
            MailRepository mailRepository,
            MailRecipientRepository mailRecipientRepository,
            AttachmentRepository attachmentRepository,
            AuthModuleInterface authModuleInterface,
            DomainEventPublisher domainEventPublisher,
            FaceService faceService,
            MailVerificationStrategyResolver strategyResolver,
            AsyncMailProcessor asyncMailProcessor,
            AttachmentValidator attachmentValidator,
            @Qualifier("eventExecutor") Executor eventExecutor
    ) {
        this.mailRepository = mailRepository;
        this.mailRecipientRepository = mailRecipientRepository;
        this.attachmentRepository = attachmentRepository;
        this.authModuleInterface = authModuleInterface;
        this.domainEventPublisher = domainEventPublisher;
        this.faceService = faceService;
        this.strategyResolver = strategyResolver;
        this.asyncMailProcessor = asyncMailProcessor;
        this.attachmentValidator = attachmentValidator;
        this.eventExecutor = eventExecutor;
    }

    /**
     * ASYNC MAIL SENDING — optimized execution order:
     *
     * SYNCHRONOUS (user waits):
     *   1. Validate mail content        — FREE, fail fast on bad input
     *   2. Validate sender exists       — 1 DB query
     *   3. Batch-validate all recipients— 1 DB query (not N individual lookups)
     *   4. Face verification            — HTTP ~500ms (only after cheap validations pass)
     *   5. Save mail entity             — 1 DB insert (need mailId for response)
     *   6. Return response              — user gets instant feedback
     *
     * ASYNCHRONOUS (after transaction commits, user already has response):
     *   7. Batch-save recipients        — 1 saveAll() instead of N individual saves
     *   8. Publish domain event         — triggers Kafka notifications asynchronously
     *
     * BEFORE: ~500ms face verify + N recipient DB lookups + N recipient inserts + event publish
     * AFTER:  ~500ms face verify + 1 batch lookup + mail save → RETURN → async recipients + event
     */
    @Override
    public SendMailResponse sendMail(UUID senderId, SendMailRequest request) {
        // 1. Validate content FIRST (free, fail fast — no I/O needed)
        validateMailRequest(request);

        // 1b. Validate attachments at boundary (file type, size)
        attachmentValidator.validate(request.getAttachments());

        // 2. Validate sender exists
        if (!authModuleInterface.userExistsById(senderId)) {
            throw new UserNotFoundException("Sender user not found", true);
        }

        // 3. BATCH: Validate ALL recipients exist in a single query (instead of N queries)
        List<String> recipientEmails = request.getRecipients().stream()
                .map(SendMailRequest.RecipientRequest::getEmail)
                .toList();
        Map<String, UUID> recipientMap = authModuleInterface.getUserIdsByEmails(recipientEmails);

        for (SendMailRequest.RecipientRequest r : request.getRecipients()) {
            if (!recipientMap.containsKey(r.getEmail())) {
                throw new UserNotFoundException(r.getEmail());
            }
        }

        // 4. Face verification (expensive HTTP call — only run after all cheap validations pass)
        boolean isVerified = faceService.verifyFace(senderId, request.getFaceFrames());
        if (!isVerified) {
            throw new InvalidMailException("Face verification failed. Cannot send mail.");
        }

        // 5. Save mail entity (synchronous — we need the ID for the response)
        Mail mail = new Mail();
        mail.setSubject(request.getSubject());
        mail.setBody(request.getBody());
        mail.setSenderId(senderId);
        mail.setIsConfidential(request.getIsConfidential() != null ? request.getIsConfidential() : false);
        Mail savedMail = mailRepository.save(mail);

        // 6. ASYNC: Schedule recipient processing + event publishing AFTER this transaction commits
        //    The user gets their response immediately — doesn't wait for N recipient inserts
        UUID mailId = savedMail.getId();
        boolean isConfidential = savedMail.getIsConfidential() != null && savedMail.getIsConfidential();
        List<SendMailRequest.RecipientRequest> recipientReqs = request.getRecipients();
        List<MultipartFile> attachments = request.getAttachments();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(
                        () -> asyncMailProcessor.processRecipientsAndNotify(
                                mailId, senderId, recipientReqs, recipientMap, isConfidential, attachments),
                        eventExecutor);
            }
        });

        // 7. Return response immediately
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
            response.setAttachments(toAttachmentResponses(mail.getId()));

            // Count recipients
            List<MailRecipient> recipients = mailRecipientRepository.findByMailIdAndIsDeletedFalse(mail.getId());
            response.setRecipientCount(recipients.size());

            return response;
        });

        return new PageResponse<>(responses);
    }

    @Override
    @CacheEvict(value = CacheNames.UNREAD_MAIL_COUNT, key = "#recipientId")
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
    @Cacheable(value = CacheNames.UNREAD_MAIL_COUNT, key = "#recipientId")
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
        response.setAttachments(toAttachmentResponses(mail.getId()));

        return response;
    }

    /**
     * DESIGN PATTERN: Strategy
     * 
     * Instead of if/else checking mail.isConfidential here, we delegate to
     * the appropriate MailVerificationStrategy selected by the StrategyResolver.
     * This makes it easy to add new strategies (OTP, PIN, etc.) without modifying this method.
     */
    @Override
    @Transactional
    public MailResponse openMail(UUID mailId, UUID recipientId, List<MultipartFile> faceFrames) {

        // Find the mail recipient entry
        MailRecipient mailRecipient = mailRecipientRepository.findByIdAndRecipientIdAndNotDeleted(
                mailId,
                recipientId
        ).orElseThrow(() -> new MailNotFoundException("Mail not found or access denied"));

        Mail mail = mailRecipient.getMail();

        // Strategy pattern: resolve and execute the correct verification strategy
        MailVerificationStrategy strategy = strategyResolver.resolve(mail);
        strategy.verify(recipientId, faceFrames);

        // Mark as read if not already
        if (!mailRecipient.getIsRead()) {
            mailRecipient.setIsRead(true);
            mailRecipient.setReadAt(LocalDateTime.now());
            mailRecipientRepository.save(mailRecipient);
        }

        // Return the mail response
        return toMailResponse(mailRecipient);
    }

    private List<AttachmentResponse> toAttachmentResponses(UUID mailId) {
        return attachmentRepository.findByMailId(mailId).stream()
                .map(a -> AttachmentResponse.builder()
                        .id(a.getId())
                        .originalFileName(a.getOriginalFileName())
                        .contentType(a.getContentType())
                        .fileSize(a.getFileSize())
                        .build())
                .toList();
    }
}
