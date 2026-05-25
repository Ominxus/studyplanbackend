package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.ChangePasswordRequest;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.UserRepository;
import com.studentplansystem.studyplangym.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/change-password")
public class ChangePasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public ChangePasswordController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();

        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Current password is required.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("New password is required.");
        }

        List<String> validationErrors = validatePassword(request.getNewPassword());

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(String.join("\n", validationErrors));
        }

        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditLogService.log(
                authentication,
                "CHANGE_PASSWORD",
                "User changed password: " + user.getUsername()
        );

        return ResponseEntity.ok("Password changed successfully.");
    }

    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long.");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one capital letter.");
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            errors.add("Password must contain at least one special character.");
        }

        return errors;
    }
}