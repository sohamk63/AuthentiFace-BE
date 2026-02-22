package com.alethia.AuthentiFace.AuthService.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.DTOs.LoginDto;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.JwtService;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.LoginService;

@Service
public class LoginServiceImp implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    @Autowired
    public LoginServiceImp(AuthenticationManager authenticationManager, JwtService jwtService){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public String login(LoginDto loginReq){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword());
        Authentication auth = authenticationManager.authenticate(usernamePasswordAuthenticationToken);   
        return jwtService.getToken(auth);
    }
}
