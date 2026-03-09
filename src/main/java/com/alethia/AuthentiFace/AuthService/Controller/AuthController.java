package com.alethia.AuthentiFace.AuthService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.LoginResponseDto;
import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.RegisterService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.LoginService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserPrincipal;
import com.alethia.AuthentiFace.AuthService.Service.impl.LoginServiceImp;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
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
