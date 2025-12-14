package com.alethia.AuthentiFace.AuthService.Service;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;

public class CustomUserDetails implements UserDetailsService {

    private final UserRepository userRepo;

    @Autowired
    public CustomUserDetails(UserRepository userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));

        return new UserPrincipal(user);
    }

}
