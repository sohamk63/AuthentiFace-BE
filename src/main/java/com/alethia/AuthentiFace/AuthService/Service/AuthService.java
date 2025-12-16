package com.alethia.AuthentiFace.AuthService.Service;

import org.springframework.security.core.Authentication;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;

public interface AuthService {
    void register(RegisterUserDto user);
    String login(LoginDto loginReq);
}
