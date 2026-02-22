package com.alethia.AuthentiFace.AuthService.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.RegisterService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.LoginService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;

    @Autowired
    public AuthController(
            RegisterService registerService,
            LoginService loginService
    ) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginDto request
    ) {
        String token = loginService.login(request);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterUserDto request
    ) {
        registerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

}
