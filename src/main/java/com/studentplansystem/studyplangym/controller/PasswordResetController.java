package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.PasswordResetRequest;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.PasswordResetRequestRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import com.studentplansystem.studyplangym.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.studentplansystem.studyplangym.dto.PasswordResetRequestResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public PasswordResetController(
            PasswordResetRequestRepository passwordResetRequestRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService
    ) {
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        String username = body.get("username");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required.");
        }

        username = username.trim();

        if (username.contains(" ")) {
            return ResponseEntity.badRequest().body("Username cannot contain spaces.");
        }

        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("No account found with this username.");
        }

        if (passwordResetRequestRepository.existsByUsernameAndStatus(username, "PENDING")) {
            return ResponseEntity.badRequest().body("A password reset request is already pending.");
        }

        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setUsername(username);
        resetRequest.setStatus("PENDING");
        resetRequest.setRequestedAt(LocalDateTime.now());

        passwordResetRequestRepository.save(resetRequest);

        return ResponseEntity.ok("Password reset request sent to admin.");
    }

    @GetMapping("/requests")
    public List<PasswordResetRequestResponse> getPendingRequests() {
        List<PasswordResetRequest> requests =
                passwordResetRequestRepository.findByStatusOrderByRequestedAtDesc("PENDING");

        List<PasswordResetRequestResponse> response = new ArrayList<>();

        for (PasswordResetRequest request : requests) {
            String email = userRepository.findByUsername(request.getUsername())
                    .map(User::getEmail)
                    .orElse("");

            response.add(
                    new PasswordResetRequestResponse(
                            request.getId(),
                            request.getUsername(),
                            email,
                            request.getStatus(),
                            request.getRequestedAt(),
                            request.getCompletedAt()
                    )
            );
        }

        return response;
    }

    @PostMapping("/complete/{requestId}")
    public ResponseEntity<?> completePasswordReset(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String newPassword = body.get("newPassword");

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("New password is required.");
        }

        List<String> validationErrors = validatePassword(newPassword);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(String.join("\n", validationErrors));
        }

        Optional<PasswordResetRequest> resetRequestOptional =
                passwordResetRequestRepository.findById(requestId);

        if (resetRequestOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PasswordResetRequest resetRequest = resetRequestOptional.get();

        if (!"PENDING".equalsIgnoreCase(resetRequest.getStatus())) {
            return ResponseEntity.badRequest().body("This request has already been handled.");
        }

        Optional<User> userOptional = userRepository.findByUsername(resetRequest.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User account no longer exists.");
        }

        User user = userOptional.get();

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        resetRequest.setStatus("COMPLETED");
        resetRequest.setCompletedAt(LocalDateTime.now());
        passwordResetRequestRepository.save(resetRequest);

        auditLogService.log(
                authentication,
                "PASSWORD_RESET",
                "Reset temporary password for user: " + user.getUsername()
        );

        return ResponseEntity.ok("Password reset completed and temporary password was emailed to the student.");
    }

    @PostMapping("/deny/{requestId}")
    public ResponseEntity<?> denyPasswordReset(
            @PathVariable Long requestId,
            Authentication authentication
    ) {
        Optional<PasswordResetRequest> resetRequestOptional =
                passwordResetRequestRepository.findById(requestId);

        if (resetRequestOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PasswordResetRequest resetRequest = resetRequestOptional.get();

        if (!"PENDING".equalsIgnoreCase(resetRequest.getStatus())) {
            return ResponseEntity.badRequest().body("This request has already been handled.");
        }

        resetRequest.setStatus("DENIED");
        resetRequest.setCompletedAt(LocalDateTime.now());
        passwordResetRequestRepository.save(resetRequest);

        auditLogService.log(
                authentication,
                "DENY_PASSWORD_RESET",
                "Denied password reset request for user: " + resetRequest.getUsername()
        );

        return ResponseEntity.ok("Password reset request denied.");
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