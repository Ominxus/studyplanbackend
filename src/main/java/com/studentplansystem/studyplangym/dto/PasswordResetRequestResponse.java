package com.studentplansystem.studyplangym.dto;

import java.time.LocalDateTime;

public class PasswordResetRequestResponse {

    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public PasswordResetRequestResponse(
            Long id,
            String username,
            String email,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}