package com.alethia.AuthentiFace.AuthService.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class RegisterUserDto {
    private String email;
    private String password;
}
