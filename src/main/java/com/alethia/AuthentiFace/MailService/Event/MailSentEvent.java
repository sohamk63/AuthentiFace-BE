package com.alethia.AuthentiFace.MailService.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MailSentEvent extends ApplicationEvent {

    private final UUID mailId;
    private final UUID senderId;
    private final List<UUID> recipientIds;
    private final LocalDateTime sentAt; // Renamed from 'timestamp' to 'sentAt'

    public MailSentEvent(
            Object source,
            UUID mailId,
            UUID senderId,
            List<UUID> recipientIds,
            LocalDateTime sentAt // Updated parameter name
    ) {
        super(source);
        this.mailId = mailId;
        this.senderId = senderId;
        this.recipientIds = recipientIds;
        this.sentAt = sentAt; // Updated field name
    }

    public MailSentEvent(
            Object source,
            UUID mailId,
            UUID senderId,
            List<UUID> recipientIds
    ) {
        this(source, mailId, senderId, recipientIds, LocalDateTime.now());
    }
}