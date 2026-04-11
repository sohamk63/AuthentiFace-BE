package com.alethia.AuthentiFace.AuthService.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.AuthService.DTOs.ChangePasswordDto;
import com.alethia.AuthentiFace.AuthService.DTOs.UserSearchResult;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private static final String PROFILE_PHOTOS_DIR = "./profile-photos";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Search users by email prefix (for compose autocomplete).
     * Returns max 10 results to keep it fast.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@RequestParam("q") String query) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<User> users = userRepository.findTop10ByEmailContainingIgnoreCase(query.trim());

        List<UserSearchResult> results = users.stream()
                .map(user -> new UserSearchResult(
                        user.getEmail(),
                        getInitials(user.getEmail()),
                        user.getProfilePhotoKey() != null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * Change the current user's password.
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordDto request) {

        User user = getCurrentUser();
        Map<String, String> response = new HashMap<>();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            response.put("message", "Current password is incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            response.put("message", "New password must be different from current password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Upload profile photo for the current user.
     */
    @PostMapping("/profile-photo")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @RequestParam("photo") MultipartFile photo) throws IOException {

        Map<String, String> response = new HashMap<>();

        if (photo.isEmpty()) {
            response.put("message", "Photo file is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (photo.getSize() > MAX_FILE_SIZE) {
            response.put("message", "Photo must be less than 5MB");
            return ResponseEntity.badRequest().body(response);
        }

        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("message", "File must be an image");
            return ResponseEntity.badRequest().body(response);
        }

        User user = getCurrentUser();

        // Delete old photo if exists
        if (user.getProfilePhotoKey() != null) {
            Path oldPath = Paths.get(PROFILE_PHOTOS_DIR).toAbsolutePath().normalize()
                    .resolve(user.getProfilePhotoKey());
            Files.deleteIfExists(oldPath);
        }

        // Store new photo
        String extension = getExtension(photo.getOriginalFilename());
        String photoKey = user.getUserId().toString() + extension;
        Path dir = Paths.get(PROFILE_PHOTOS_DIR).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path filePath = dir.resolve(photoKey);
        photo.transferTo(filePath.toFile());

        user.setProfilePhotoKey(photoKey);
        userRepository.save(user);

        response.put("message", "Profile photo updated successfully");
        response.put("photoKey", photoKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile photo for a user by email.
     */
    @GetMapping("/profile-photo")
    public ResponseEntity<byte[]> getProfilePhoto(@RequestParam(value = "email", required = false) String email)
            throws IOException {

        User user;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        } else {
            user = getCurrentUser();
        }

        if (user == null || user.getProfilePhotoKey() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(PROFILE_PHOTOS_DIR).toAbsolutePath().normalize()
                .resolve(user.getProfilePhotoKey());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] photoBytes = Files.readAllBytes(filePath);
        String extension = getExtension(user.getProfilePhotoKey());
        MediaType mediaType = extension.equals(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(photoBytes);
    }

    /**
     * Get current user's profile info.
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        User user = getCurrentUser();

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", user.getEmail());
        profile.put("initials", getInitials(user.getEmail()));
        profile.put("hasProfilePhoto", user.getProfilePhotoKey() != null);
        profile.put("faceEnrolled", Boolean.TRUE.equals(user.getFaceEnrolled()));
        profile.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(profile);
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUser();
    }

    private String getInitials(String email) {
        String name = email.split("@")[0];
        if (name.length() <= 2) return name.toUpperCase();
        return (String.valueOf(name.charAt(0)) + name.charAt(name.length() - 1)).toUpperCase();
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
