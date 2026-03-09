package com.alethia.AuthentiFace.FaceVerificationService.DTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyFaceResponse {

    private UUID userId;
    private boolean verified;
    private String message;

    public VerifyFaceResponse(UUID userId, boolean verified) {
        this.userId = userId;
        this.verified = verified;
        this.message = verified ? "Face verified successfully" : "Face verification failed";
    }
}
