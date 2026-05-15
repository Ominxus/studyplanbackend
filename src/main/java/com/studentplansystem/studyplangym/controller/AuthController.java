package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.LoginRequest;
import com.studentplansystem.studyplangym.dto.LoginResponse;
import com.studentplansystem.studyplangym.dto.RegisterRequest;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.UserRepository;
import com.studentplansystem.studyplangym.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        String rawPassword = request.getPassword();

        if (username.contains(" ")) {
            return ResponseEntity.badRequest().body("Username cannot contain spaces");
        }

        if (username.length() > 20) {
            return ResponseEntity.badRequest().body("Username cannot be more than 20 characters");
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        User foundUser = user.get();
        String storedPassword = foundUser.getPassword();

        boolean passwordMatches;

        if (isBCryptHash(storedPassword)) {
            passwordMatches = passwordEncoder.matches(rawPassword, storedPassword);
        } else {
            passwordMatches = rawPassword.equals(storedPassword);

            if (passwordMatches) {
                foundUser.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.save(foundUser);
            }
        }

        if (!passwordMatches) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        String token = jwtUtil.generateToken(foundUser.getUsername(), foundUser.getRole());

        return ResponseEntity.ok(
                new LoginResponse(foundUser.getUsername(), foundUser.getRole(), token)
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
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("STUDENT");

        userRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole());

        return ResponseEntity.ok(
                new LoginResponse(newUser.getUsername(), newUser.getRole(), token)
        );
    }

    private boolean isBCryptHash(String password) {
        return password != null &&
                (password.startsWith("$2a$") ||
                        password.startsWith("$2b$") ||
                        password.startsWith("$2y$"));
    }
}