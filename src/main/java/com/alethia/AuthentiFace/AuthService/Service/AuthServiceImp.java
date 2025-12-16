package com.alethia.AuthentiFace.AuthService.Service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.Roles;
import com.alethia.AuthentiFace.AuthService.Entities.User;

@Service
public class AuthServiceImp implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
   
    
    @Autowired
    public AuthServiceImp(PasswordEncoder passwordEncoder, UserService userService, AuthenticationManager authenticationManager, JwtService jwtService){
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
    public String login(LoginDto loginReq){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword());
        Authentication auth = authenticationManager.authenticate(usernamePasswordAuthenticationToken);   
        return jwtService.getToken(auth);
    }
}
