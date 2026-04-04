package com.alethia.AuthentiFace.AuthService.Service.impl;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.Roles;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.RegisterService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserService;

@Service
public class RegisterServiceImp implements RegisterService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    
    @Autowired
    public RegisterServiceImp(PasswordEncoder passwordEncoder, UserService userService){
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
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
}
