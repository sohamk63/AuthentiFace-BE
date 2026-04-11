package com.alethia.AuthentiFace.MailService.Service.Impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.MailService.Exception.InvalidMailException;

@Component
public class AttachmentValidator {

    private static final long MAX_SINGLE_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_SIZE = 25 * 1024 * 1024; // 25MB
    private static final int MAX_ATTACHMENT_COUNT = 10;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            // Images
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            // Archives
            "application/zip",
            "application/x-rar-compressed",
            "application/gzip",
            "application/x-7z-compressed",
            // Code/config
            "application/json",
            "application/xml",
            "text/xml",
            "text/html",
            // Media
            "audio/mpeg",
            "audio/wav",
            "video/mp4",
            "video/webm"
    );

    public void validate(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        if (attachments.size() > MAX_ATTACHMENT_COUNT) {
            throw new InvalidMailException("Maximum " + MAX_ATTACHMENT_COUNT + " attachments allowed");
        }

        long totalSize = 0;
        for (MultipartFile file : attachments) {
            if (file.isEmpty()) {
                throw new InvalidMailException("Empty attachment file detected");
            }

            if (file.getSize() > MAX_SINGLE_FILE_SIZE) {
                throw new InvalidMailException("Attachment '" + file.getOriginalFilename()
                        + "' exceeds maximum size of 10MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new InvalidMailException("File type '" + contentType
                        + "' is not allowed for attachment '" + file.getOriginalFilename() + "'");
            }

            totalSize += file.getSize();
        }

        if (totalSize > MAX_TOTAL_SIZE) {
            throw new InvalidMailException("Total attachment size exceeds maximum of 25MB");
        }
    }
}
