package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.LoginRequest;
import com.studentplansystem.studyplangym.dto.LoginResponse;
import com.studentplansystem.studyplangym.dto.RegisterRequest;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        String username = request.getUsername().trim();

        if (username.contains(" ")) {
            return ResponseEntity.badRequest().body("Username cannot contain spaces");
        }

        if (username.length() > 20) {
            return ResponseEntity.badRequest().body("Username cannot be more than 20 characters");
        }

        Optional<User> user = userRepository.findByUsernameAndPassword(
                username,
                request.getPassword()
        );

        if (user.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        User foundUser = user.get();

        return ResponseEntity.ok(
                new LoginResponse(foundUser.getUsername(), foundUser.getRole())
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        if (username.contains(" ")) {
            return ResponseEntity.badRequest().body("Username cannot contain spaces");
        }

        if (username.length() > 20) {
            return ResponseEntity.badRequest().body("Username cannot be more than 20 characters");
        }

        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            return ResponseEntity.badRequest().body("Password must contain at least one capital letter");
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return ResponseEntity.badRequest().body("Password must contain at least one special character");
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole("STUDENT");

        userRepository.save(newUser);

        return ResponseEntity.ok(
                new LoginResponse(newUser.getUsername(), newUser.getRole())
        );
    }
}