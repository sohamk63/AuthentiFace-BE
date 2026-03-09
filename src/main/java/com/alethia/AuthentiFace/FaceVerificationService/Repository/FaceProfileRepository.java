package com.alethia.AuthentiFace.FaceVerificationService.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alethia.AuthentiFace.FaceVerificationService.Entity.FaceProfile;

@Repository
public interface FaceProfileRepository extends JpaRepository<FaceProfile, UUID> {

    Optional<FaceProfile> findByUserIdAndIsActiveTrue(UUID userId);

}
