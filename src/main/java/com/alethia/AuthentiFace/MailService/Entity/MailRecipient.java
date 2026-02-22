package com.alethia.AuthentiFace.MailService.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mail_recipient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailRecipient {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id", nullable = false)
    private Mail mail;

    @Column(nullable = false, name = "recipient_id")
    private UUID recipientId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RecipientType type;

    @Column(columnDefinition = "boolean default false")
    private Boolean isRead = false;

    @Column(nullable = true)
    private LocalDateTime readAt;

    @Column(columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isRead = false;
        isDeleted = false;
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RecipientType {
        TO, CC, BCC
    }
}
