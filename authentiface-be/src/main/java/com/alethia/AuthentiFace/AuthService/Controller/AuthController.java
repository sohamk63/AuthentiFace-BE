package com.alethia.AuthentiFace.AuthService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;

import java.util.List;
import com.alethia.AuthentiFace.AuthService.DTOs.LoginResponseDto;
import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.RegisterService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.LoginService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserPrincipal;
import com.alethia.AuthentiFace.AuthService.Service.impl.LoginServiceImp;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final LoginServiceImp loginServiceImp;

    @Autowired
    public AuthController(
            RegisterService registerService,
            LoginService loginService,
            LoginServiceImp loginServiceImp
    ) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.loginServiceImp = loginServiceImp;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginDto request
    ) {
        String token = loginService.login(request);
        User user = loginServiceImp.getUserByEmail(request.getEmail());
        
        LoginResponseDto response = new LoginResponseDto(token, user.getFaceEnrolled());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterUserDto request
    ) {
        registerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/face-login")
    public ResponseEntity<?> faceLogin(
            @RequestParam("email") String email,
            @RequestParam("frames") List<MultipartFile> frames
    ) {
        try {
            LoginResponseDto response = loginService.faceLogin(email, frames);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            if ("FACE_NOT_ENROLLED".equals(ex.getMessage())) {
                error.put("error", "FACE_NOT_ENROLLED");
                error.put("message", "Face not enrolled. Please login with password first and enroll your face.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (ex.getMessage() != null && ex.getMessage().contains("User not found")) {
                error.put("error", "USER_NOT_FOUND");
                error.put("message", "No account found with this email.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                error.put("error", "FACE_VERIFICATION_FAILED");
                error.put("message", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        }
    }

    @GetMapping("/face-enrollment-status")
    public ResponseEntity<Map<String, Boolean>> checkFaceEnrollmentStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("faceEnrolled", user.getFaceEnrolled());
        
        return ResponseEntity.ok(response);
    }

}
