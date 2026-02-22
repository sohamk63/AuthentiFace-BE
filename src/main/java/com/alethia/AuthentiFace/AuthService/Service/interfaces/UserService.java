package com.alethia.AuthentiFace.AuthService.Service.interfaces;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;


public interface UserService {
    User saveUser(User regUser);
    Optional<User> findByEmail(String email);
}
