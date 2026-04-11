package com.alethia.AuthentiFace.AuthService.Service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.Entities.User;
import com.alethia.AuthentiFace.AuthService.Repository.UserRepository;
import com.alethia.AuthentiFace.MailService.Service.AuthModuleInterface;

/**
 * Implementation of AuthModuleInterface for Mail module's use.
 * This allows Mail module to validate users without directly accessing UserRepository.
 */
@Service
public class AuthModuleImpl implements AuthModuleInterface {

    private final UserRepository userRepository;

    @Autowired
    public AuthModuleImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean userExistsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public Optional<String> getUserEmailById(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getEmail);
    }

    @Override
    public Optional<UUID> getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUserId);
    }

    @Override
    public Map<String, UUID> getUserIdsByEmails(List<String> emails) {
        return userRepository.findByEmailIn(emails).stream()
                .collect(Collectors.toMap(User::getEmail, User::getUserId));
    }
}
