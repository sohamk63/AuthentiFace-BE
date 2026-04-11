package com.alethia.AuthentiFace.AuthService.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResult {
    private String email;
    private String initials;
    private boolean hasProfilePhoto;
}
