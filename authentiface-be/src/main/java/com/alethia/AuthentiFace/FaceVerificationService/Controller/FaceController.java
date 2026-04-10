package com.alethia.AuthentiFace.FaceVerificationService.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;
import com.alethia.AuthentiFace.FaceVerificationService.DTO.EnrollFaceResponse;
import com.alethia.AuthentiFace.FaceVerificationService.DTO.VerifyFaceResponse;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceService;

@RestController
@RequestMapping("/api/face")
public class FaceController {

    private final FaceService faceService;
    private final UserRepository userRepository;

    public FaceController(FaceService faceService, UserRepository userRepository) {
        this.faceService = faceService;
        this.userRepository = userRepository;
    }

    @PostMapping("/enroll")
    public ResponseEntity<EnrollFaceResponse> enrollFace(
            @RequestParam("frames") List<MultipartFile> frames) {

        // Extract authenticated user from SecurityContext
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        
        String email = userDetails.getUsername();
        
        // Look up user by email to get userId
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        UUID userId = userOpt.get().getUserId();
        
        // Enroll face for authenticated user
        faceService.enrollFace(userId, frames);
        EnrollFaceResponse response = new EnrollFaceResponse(userId, true);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyFaceResponse> verifyFace(
            @RequestParam UUID userId,
            @RequestParam("frames") List<MultipartFile> frames) {

        boolean verified = faceService.verifyFace(userId, frames);
        VerifyFaceResponse response = new VerifyFaceResponse(userId, verified);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
