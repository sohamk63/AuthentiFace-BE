package com.alethia.AuthentiFace.AuthService.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.alethia.AuthentiFace.AuthService.DTOs.RegisterUserDto;
import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;

public class UserServiceImp implements UserService {

    private final PasswordEncoder pe;
    private final UserRepository userRepo;
    
    @Autowired
    public UserServiceImp(PasswordEncoder pe, UserRepository userRepo){
        this.pe = pe;
        this.userRepo = userRepo;
    }

    @Override
    public User register(RegisterUserDto userDto){
        User u = new User();
        u.setEmail(userDto.getEmail());
        u.setCreatedAt(LocalDateTime.now());
        u.setPassword(pe.encode(userDto.getPassword()));
        u.setRole("user");
               
        return userRepo.save(u);
    }

    @Override
    public Optional<User> findByEmail(String email){
        return userRepo.findByEmail(email);
    }
}
