package com.alethia.AuthentiFace.AuthService.Service.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.DTOs.LoginResponseDto;

public interface LoginService {
    String login(LoginDto loginReq);
    LoginResponseDto faceLogin(String email, List<MultipartFile> frames);
}
