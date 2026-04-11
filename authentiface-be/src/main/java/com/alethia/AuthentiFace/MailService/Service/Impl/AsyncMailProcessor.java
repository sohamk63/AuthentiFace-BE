package com.alethia.AuthentiFace.MailService.Service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.Common.Event.DomainEventPublisher;
import com.alethia.AuthentiFace.Common.Event.MailSentDomainEvent;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest.RecipientRequest;
import com.alethia.AuthentiFace.MailService.Entity.Attachment;
import com.alethia.AuthentiFace.MailService.Entity.Mail;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient;
import com.alethia.AuthentiFace.MailService.Entity.MailRecipient.RecipientType;
import com.alethia.AuthentiFace.MailService.Repository.AttachmentRepository;
import com.alethia.AuthentiFace.MailService.Repository.MailRecipientRepository;
import com.alethia.AuthentiFace.MailService.Repository.MailRepository;
import com.alethia.AuthentiFace.MailService.Service.AttachmentStorageService;
import com.alethia.AuthentiFace.MailService.Service.AuthModuleInterface;

/**
 * ASYNC: Handles recipient processing and event publishing in a separate thread/transaction.
 * 
 * Called by MailServiceImpl AFTER the mail-save transaction commits, so:
 * - The mail row exists in the DB (FK constraint satisfied)
 * - Recipients are batch-saved in one flush (saveAll instead of N individual saves)
 * - The response has already been returned to the user (fast UX)
 */
@Component
public class AsyncMailProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncMailProcessor.class);

    private final MailRepository mailRepository;
    private final MailRecipientRepository mailRecipientRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentStorageService attachmentStorageService;
    private final AuthModuleInterface authModuleInterface;
    private final DomainEventPublisher domainEventPublisher;
    private final Executor eventExecutor;

    public AsyncMailProcessor(MailRepository mailRepository,
            MailRecipientRepository mailRecipientRepository,
            AttachmentRepository attachmentRepository,
            AttachmentStorageService attachmentStorageService,
            AuthModuleInterface authModuleInterface,
            DomainEventPublisher domainEventPublisher,
            @Qualifier("eventExecutor") Executor eventExecutor) {
        this.mailRepository = mailRepository;
        this.mailRecipientRepository = mailRecipientRepository;
        this.attachmentRepository = attachmentRepository;
        this.attachmentStorageService = attachmentStorageService;
        this.authModuleInterface = authModuleInterface;
        this.domainEventPublisher = domainEventPublisher;
        this.eventExecutor = eventExecutor;
    }

    /**
     * Process recipients and publish domain event.
     * Runs in a NEW transaction (the calling transaction has already committed).
     *
     * @param mailId       the persisted mail ID
     * @param senderId     sender's user ID
     * @param recipients   recipient requests from the original request
     * @param recipientMap pre-resolved email → userId map (batch-queried by caller)
     * @param isConfidential whether the mail is confidential
     */
    @Transactional
    public void processRecipientsAndNotify(UUID mailId, UUID senderId,
            List<RecipientRequest> recipients, Map<String, UUID> recipientMap,
            boolean isConfidential, List<MultipartFile> attachments) {
        try {
            Mail mail = mailRepository.getReferenceById(mailId);

            // Store attachments in parallel using multithreading (I/O bound)
            if (attachments != null && !attachments.isEmpty()) {
                storeAttachments(mail, attachments);
            }

            List<MailRecipient> recipientEntities = new ArrayList<>();
            List<UUID> recipientIds = new ArrayList<>();

            for (RecipientRequest recipientReq : recipients) {
                UUID recipientId = recipientMap.get(recipientReq.getEmail());

                MailRecipient recipient = new MailRecipient();
                recipient.setMail(mail);
                recipient.setRecipientId(recipientId);
                recipient.setType(RecipientType.valueOf(recipientReq.getType().toUpperCase()));
                recipient.setIsRead(false);
                recipient.setIsDeleted(false);

                recipientEntities.add(recipient);
                recipientIds.add(recipientId);
            }

            // BATCH: Single saveAll instead of N individual save() calls
            mailRecipientRepository.saveAll(recipientEntities);

            // Publish domain event (listeners handle Kafka asynchronously)
            String senderEmail = authModuleInterface.getUserEmailById(senderId).orElse("unknown");
            domainEventPublisher.publish(new MailSentDomainEvent(
                    senderId, mailId, senderEmail, recipientIds, isConfidential));

            log.info("[async] Processed {} recipients for mail {}", recipientIds.size(), mailId);

        } catch (Exception ex) {
            log.error("[async] Failed to process recipients for mail {}: {}",
                    mailId, ex.getMessage(), ex);
        }
    }

    private void storeAttachments(Mail mail, List<MultipartFile> attachments) {
        // Fan-out: store each file on a separate thread (I/O bound — disk writes)
        List<CompletableFuture<Attachment>> futures = attachments.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    String storageKey = attachmentStorageService.store(file);
                    return Attachment.builder()
                            .mail(mail)
                            .originalFileName(file.getOriginalFilename())
                            .storageKey(storageKey)
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .build();
                }, eventExecutor))
                .toList();

        // Join all — wait for parallel storage to complete
        List<Attachment> entities = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        attachmentRepository.saveAll(entities);
        log.info("[async] Stored {} attachments for mail {}", entities.size(), mail.getId());
    }
}
