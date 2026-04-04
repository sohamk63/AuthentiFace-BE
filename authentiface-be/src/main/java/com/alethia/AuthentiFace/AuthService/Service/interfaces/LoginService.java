package com.alethia.AuthentiFace.AuthService.Service.interfaces;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;

public interface LoginService {
    String login(LoginDto loginReq);
}
