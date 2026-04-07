package com.petal.service;

import com.petal.dto.AuthRequest;
import com.petal.dto.AuthResponse;
import com.petal.dto.RegisterRequest;
import com.petal.entity.User;
import com.petal.factory.UserFactory;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public boolean registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return false;
        }

        // Delegate to Factory
        User user = UserFactory.createUser(request, passwordEncoder);
        userRepository.save(user);
        return true;
    }

    public AuthResponse loginUser(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return null;
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
