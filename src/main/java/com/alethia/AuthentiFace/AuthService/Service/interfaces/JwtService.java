package com.alethia.AuthentiFace.AuthService.Service.interfaces;

import org.springframework.security.core.Authentication;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String getToken(Authentication authToken);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    
}
