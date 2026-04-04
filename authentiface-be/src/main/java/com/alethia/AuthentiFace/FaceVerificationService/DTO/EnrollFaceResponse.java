package com.alethia.AuthentiFace.FaceVerificationService.DTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollFaceResponse {

    private UUID userId;
    private String message;
    private boolean success;

    public EnrollFaceResponse(UUID userId, boolean success) {
        this.userId = userId;
        this.success = success;
        this.message = success ? "Face enrolled successfully" : "Face enrollment failed";
    }
}
