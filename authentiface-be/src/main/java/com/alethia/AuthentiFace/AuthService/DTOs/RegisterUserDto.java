package com.alethia.AuthentiFace.AuthService.DTOs;


import lombok.Data;

@Data
public class RegisterUserDto {
    private String email;
    private String password;
}
