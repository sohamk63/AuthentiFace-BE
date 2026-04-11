package com.alethia.AuthentiFace.MailService.DTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailResponse {

    private UUID id;

    private String subject;

    private String body;

    private String senderEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isRead;

    private LocalDateTime readAt;

    private Boolean isConfidential;

    private List<AttachmentResponse> attachments;
}
