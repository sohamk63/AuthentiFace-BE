package com.alethia.AuthentiFace.AuthService.Service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.LoginResponseDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.JwtService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.LoginService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserPrincipal;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserService;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceService;
import com.alethia.AuthentiFace.Common.Event.DomainEventPublisher;
import com.alethia.AuthentiFace.Common.Event.LoginSuccessDomainEvent;
import com.alethia.AuthentiFace.Common.Event.LoginFailedDomainEvent;

@Service
public class LoginServiceImp implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final DomainEventPublisher domainEventPublisher;
    private final FaceService faceService;
    
    @Autowired
    public LoginServiceImp(AuthenticationManager authenticationManager, JwtService jwtService,
                           UserService userService, DomainEventPublisher domainEventPublisher,
                           FaceService faceService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.domainEventPublisher = domainEventPublisher;
        this.faceService = faceService;
    }

    @Override
    public String login(LoginDto loginReq){
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword());
            Authentication auth = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            String token = jwtService.getToken(auth);

            // Observer pattern: publish domain event instead of calling Kafka directly
            Optional<User> user = userService.findByEmail(loginReq.getEmail());
            user.ifPresent(u -> domainEventPublisher.publish(
                    new LoginSuccessDomainEvent(u.getUserId(), u.getEmail())));

            return token;
        } catch (BadCredentialsException ex) {
            // Observer pattern: publish login failed domain event
            domainEventPublisher.publish(
                    new LoginFailedDomainEvent(loginReq.getEmail(), "Invalid credentials"));
            throw ex;
        }
    }

    @Override
    public LoginResponseDto faceLogin(String email, List<MultipartFile> frames) {
        // 1. Find user
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2. Check if face is enrolled
        if (user.getFaceEnrolled() == null || !user.getFaceEnrolled()) {
            domainEventPublisher.publish(new LoginFailedDomainEvent(email, "Face not enrolled"));
            throw new RuntimeException("FACE_NOT_ENROLLED");
        }

        // 3. Verify face
        boolean verified = faceService.verifyFace(user.getUserId(), frames);
        if (!verified) {
            domainEventPublisher.publish(new LoginFailedDomainEvent(email, "Face verification failed"));
            throw new RuntimeException("Face verification failed. Please try again.");
        }

        // 4. Generate JWT token
        Collection<GrantedAuthority> authorities = user.getRole().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.name()))
                .toList();
        UserPrincipal principal = new UserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        String token = jwtService.getToken(auth);

        // 5. Observer pattern: publish login success domain event
        domainEventPublisher.publish(new LoginSuccessDomainEvent(user.getUserId(), user.getEmail()));

        return new LoginResponseDto(token, user.getFaceEnrolled());
    }

    /**
     * Get user by email (used for retrieving user details after login)
     */
    public User getUserByEmail(String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.orElseThrow(() -> new RuntimeException("User not found"));
    }
}
