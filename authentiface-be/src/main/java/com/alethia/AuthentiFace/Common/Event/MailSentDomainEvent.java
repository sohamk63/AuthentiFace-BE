package com.alethia.AuthentiFace.Common.Event;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when a mail is successfully sent.
 * Observers (listeners) can react to send Kafka events, update counters, etc.
 */
public class MailSentDomainEvent extends DomainEvent {

    private final UUID mailId;
    private final String senderEmail;
    private final List<UUID> recipientIds;
    private final boolean confidential;

    public MailSentDomainEvent(UUID senderId, UUID mailId, String senderEmail,
                               List<UUID> recipientIds, boolean confidential) {
        super(senderId, "mail-service", Map.of(
                "mailId", mailId.toString(),
                "senderEmail", senderEmail,
                "recipientCount", recipientIds.size()
        ));
        this.mailId = mailId;
        this.senderEmail = senderEmail;
        this.recipientIds = recipientIds;
        this.confidential = confidential;
    }

    public UUID getMailId() { return mailId; }
    public String getSenderEmail() { return senderEmail; }
    public List<UUID> getRecipientIds() { return recipientIds; }
    public boolean isConfidential() { return confidential; }
}
