package com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface FaceImageStorageService {

    /**
     * Store the face image file and return a unique key/path
     * 
     * @param file the image file to store
     * @return unique key/path for the stored file
     */
    String store(MultipartFile file);

    /**
     * Delete the stored image by key
     * 
     * @param key the key/path of the stored file
     */
    void delete(String key);

    /**
     * Retrieve the stored image by key
     * 
     * @param key the key/path of the stored file
     * @return the image data as byte array
     */
    byte[] retrieve(String key);
}
