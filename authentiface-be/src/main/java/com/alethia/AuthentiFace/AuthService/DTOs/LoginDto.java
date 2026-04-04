package com.alethia.AuthentiFace.AuthService.DTOs;

import lombok.Data;

@Data
public class LoginDto {
    private String email;
    private String password;
}
