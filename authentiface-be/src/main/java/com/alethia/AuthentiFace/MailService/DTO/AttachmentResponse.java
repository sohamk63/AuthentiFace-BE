package com.alethia.AuthentiFace.MailService.DTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private UUID id;
    private String originalFileName;
    private String contentType;
    private long fileSize;
}
