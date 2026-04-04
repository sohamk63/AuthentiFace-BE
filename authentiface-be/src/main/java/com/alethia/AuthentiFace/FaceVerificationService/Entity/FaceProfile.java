package com.alethia.AuthentiFace.FaceVerificationService.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "face_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 255, name = "face_key")
    private String faceKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String embedding;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isActive = true;
    }
}
