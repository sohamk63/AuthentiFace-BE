package com.alethia.AuthentiFace.AuthService.Service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.JwtService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.LoginService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserService;
import com.alethia.AuthentiFace.Kafka.producer.KafkaEventPublisher;

@Service
public class LoginServiceImp implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final KafkaEventPublisher kafkaEventPublisher;
    
    @Autowired
    public LoginServiceImp(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, KafkaEventPublisher kafkaEventPublisher){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Override
    public String login(LoginDto loginReq){
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword());
            Authentication auth = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            String token = jwtService.getToken(auth);

            // Publish login success event
            Optional<User> user = userService.findByEmail(loginReq.getEmail());
            user.ifPresent(u -> kafkaEventPublisher.publishLoginSuccess(u.getUserId(), u.getEmail()));

            return token;
        } catch (BadCredentialsException ex) {
            // Publish login failed event
            kafkaEventPublisher.publishLoginFailed(loginReq.getEmail(), "Invalid credentials");
            throw ex;
        }
    }

    /**
     * Get user by email (used for retrieving user details after login)
     */
    public User getUserByEmail(String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.orElseThrow(() -> new RuntimeException("User not found"));
    }
}
