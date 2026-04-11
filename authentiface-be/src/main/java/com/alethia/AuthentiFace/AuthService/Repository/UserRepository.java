package com.alethia.AuthentiFace.AuthService.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alethia.AuthentiFace.AuthService.Entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByEmailIn(List<String> emails);

    List<User> findTop10ByEmailContainingIgnoreCase(String query);
}
