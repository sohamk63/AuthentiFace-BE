package com.alethia.AuthentiFace.MailService.Service;

import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorageService {
    String store(MultipartFile file);
    byte[] retrieve(String storageKey);
    void delete(String storageKey);
}
