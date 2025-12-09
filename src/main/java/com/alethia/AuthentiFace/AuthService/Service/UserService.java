package com.alethia.AuthentiFace.AuthService.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;


public interface UserService {
    User register(RegisterUserDto user);
    Optional<User> findByEmail(String email);
}
