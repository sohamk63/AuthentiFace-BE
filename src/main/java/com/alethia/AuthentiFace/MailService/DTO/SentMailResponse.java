package com.alethia.AuthentiFace.MailService.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentMailResponse {

    private UUID id;

    private String subject;

    private String body;

    private int recipientCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isConfidential;
}
