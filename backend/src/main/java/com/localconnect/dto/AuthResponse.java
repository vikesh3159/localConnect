package com.localconnect.dto;

import java.util.Set;

public class AuthResponse {
    private String token;
    private Long userId;
    private String phone;
    private String fullName;
    private String currentRole;
    private Set<String> roles;
    private String preferredLanguage;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String phone, String fullName, String currentRole, Set<String> roles,
            String preferredLanguage) {
        this.token = token;
        this.userId = userId;
        this.phone = phone;
        this.fullName = fullName;
        this.currentRole = currentRole;
        this.roles = roles;
        this.preferredLanguage = preferredLanguage;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }
}
