package com.studentplansystem.studyplangym.dto;

public class LoginResponse {
    private String username;
    private String role;
    private String token;
    private boolean mustChangePassword;

    public LoginResponse(String username, String role, String token, boolean mustChangePassword) {
        this.username = username;
        this.role = role;
        this.token = token;
        this.mustChangePassword = mustChangePassword;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
}