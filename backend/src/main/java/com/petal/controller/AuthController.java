package com.petal.controller;

import com.petal.dto.AuthRequest;
import com.petal.dto.AuthResponse;
import com.petal.dto.ApiResponse;
import com.petal.dto.RegisterRequest;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.FloristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final FloristService floristService;

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
                // Check if email already exists
                if (userRepository.existsByEmail(request.getEmail())) {
                        return ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Email already registered")
                                                        .build());
                }

                // Map role
                String userRole = "ROLE_BUYER"; // Default
                if (request.getRole() != null) {
                        if (request.getRole().equalsIgnoreCase("artisan")) {
                                userRole = "ROLE_FLORIST";
                        } else if (request.getRole().equalsIgnoreCase("customer")) {
                                userRole = "ROLE_BUYER";
                        }
                }

                // Create and save user with encrypted password
                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(userRole)
                                .build();

                User savedUser = userRepository.save(user);

                if ("ROLE_FLORIST".equals(userRole)) {
                        floristService.createDefaultProfile(savedUser);
                }

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.<Void>builder()
                                                .success(true)
                                                .message("Registration successful")
                                                .build());
        }

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
                // Find user by email
                User user = userRepository.findByEmail(request.getEmail())
                                .orElse(null);

                if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.<AuthResponse>builder()
                                                        .success(false)
                                                        .message("Invalid email or password")
                                                        .build());
                }

                // Generate JWT token
                String token = jwtUtil.generateToken(user.getEmail());

                AuthResponse response = AuthResponse.builder()
                                .token(token)
                                .name(user.getName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build();

                return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                                .success(true)
                                .message("Login successful")
                                .data(response)
                                .build());
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout() {
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Logged out successfully")
                                .build());
        }
}
