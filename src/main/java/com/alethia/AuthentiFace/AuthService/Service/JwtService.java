package com.alethia.AuthentiFace.AuthService.Service;

import org.springframework.security.core.Authentication;

public interface JwtService {
    String getToken(Authentication authToken);
    
}
