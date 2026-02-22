package com.alethia.AuthentiFace.AuthService.Service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;
import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserService;

@Service
public class UserServiceImp implements UserService {  
    private final UserRepository userRepo;
    
    @Autowired
    public UserServiceImp( UserRepository userRepo){
        this.userRepo = userRepo;
    }
    @Override
    public Optional<User> findByEmail(String email){
        return userRepo.findByEmail(email);
    }
    @Override
    public User saveUser(User regUser){
        return userRepo.save(regUser);
    }
}
