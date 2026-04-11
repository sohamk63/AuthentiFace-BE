package com.alethia.AuthentiFace.MailService.Service.Impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.MailService.Exception.MailException;
import com.alethia.AuthentiFace.MailService.Service.AttachmentStorageService;

@Service
public class LocalAttachmentStorageService implements AttachmentStorageService {

    @Value("${attachment.storage.local.path:./attachments}")
    private String storagePath;

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new MailException("Cannot store empty file");
        }

        try {
            Path storageDir = Paths.get(storagePath);
            Files.createDirectories(storageDir);

            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String uniqueName = UUID.randomUUID() + extension;

            Path filePath = storageDir.resolve(uniqueName);
            Files.write(filePath, file.getBytes());

            return uniqueName;
        } catch (IOException e) {
            throw new MailException("Failed to store attachment: " + e.getMessage());
        }
    }

    @Override
    public byte[] retrieve(String storageKey) {
        try {
            Path filePath = Paths.get(storagePath).resolve(storageKey);
            if (!Files.exists(filePath)) {
                throw new MailException("Attachment not found: " + storageKey);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new MailException("Failed to retrieve attachment: " + e.getMessage());
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path filePath = Paths.get(storagePath).resolve(storageKey);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new MailException("Failed to delete attachment: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
