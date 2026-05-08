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
@CrossOrigin(origins = {"http://localhost:5173", "https://studyplanfrontend.vercel.app"})
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> user = userRepository.findByUsernameAndPassword(
                request.getUsername(),
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

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());
        newUser.setRole("STUDENT");

        userRepository.save(newUser);

        return ResponseEntity.ok(
                new LoginResponse(newUser.getUsername(), newUser.getRole())
        );
    }
}