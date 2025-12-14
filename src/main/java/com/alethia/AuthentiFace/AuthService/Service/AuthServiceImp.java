package com.alethia.AuthentiFace.AuthService.Service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.Roles;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;

@Service
public class AuthServiceImp implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
   
    
    @Autowired
    public AuthServiceImp(PasswordEncoder passwordEncoder, UserService userService, AuthenticationManager authenticationManager){
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void register(RegisterUserDto userDto) {
        if(userService.findByEmail(userDto.getEmail()).isPresent()){
            throw new RuntimeException("Email is already registered!"); 
        }
        User u = new User();
        u.setEmail(userDto.getEmail());
        u.setCreatedAt(LocalDateTime.now());
        u.setPassword(passwordEncoder.encode(userDto.getPassword()));
        u.setRole(Set.of(Roles.USER));       
        userService.saveUser(u);
    }

    @Override
    public Authentication login(LoginDto loginReq){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword());
        Authentication auth = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        return auth;
    }
}
