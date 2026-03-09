package com.alethia.AuthentiFace.FaceVerificationService.DTO;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyFaceRequest {

    @NotNull(message = "userId cannot be null")
    private UUID userId;
}
