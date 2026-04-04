package com.alethia.AuthentiFace.AuthService.Service.interfaces;

import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.alethia.AuthentiFace.AuthService.Entities.User;


public class UserPrincipal implements UserDetails {

    private final User user;
    
    public UserPrincipal(User user){
        this.user = user;
    }

    public UUID getUserId() {
        return user.getUserId();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return user.getRole().stream().map(role -> new SimpleGrantedAuthority(role.name())).toList();
    }

    @Override
    public String getPassword(){
        return this.user.getPassword();
    }

    @Override
    public String getUsername(){
        return this.user.getEmail();
    }

     @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
}
