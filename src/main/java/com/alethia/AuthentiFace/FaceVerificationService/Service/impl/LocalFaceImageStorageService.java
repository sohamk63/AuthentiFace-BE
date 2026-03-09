package com.alethia.AuthentiFace.FaceVerificationService.Service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.FaceVerificationService.Exception.FaceException;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceImageStorageService;

@Service
public class LocalFaceImageStorageService implements FaceImageStorageService {

    @Value("${face.storage.local.path}")
    private String storagePath;

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FaceException("Cannot store empty file");
        }

        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(storagePath);
            Files.createDirectories(storageDir);

            // Generate unique file name using UUID + preserve original extension
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String uniqueName = UUID.randomUUID() + extension;

            // Write file to disk
            Path filePath = storageDir.resolve(uniqueName);
            Files.write(filePath, file.getBytes());

            // Return the key as the file path from storage root
            return uniqueName;
        } catch (IOException e) {
            throw new FaceException("Failed to store face image: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path filePath = Paths.get(storagePath).resolve(key);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new FaceException("Failed to delete face image: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] retrieve(String key) {
        try {
            Path filePath = Paths.get(storagePath).resolve(key);
            if (!Files.exists(filePath)) {
                throw new FaceException("Face image not found: " + key);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FaceException("Failed to retrieve face image: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ""; // No extension
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
