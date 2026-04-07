package com.petal.factory;

import com.petal.dto.RegisterRequest;
import com.petal.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserFactory {

    public static User createUser(RegisterRequest request, PasswordEncoder encoder) {
        String userRole = "ROLE_BUYER"; // Default

        if (request.getRole() != null) {
            if (request.getRole().equalsIgnoreCase("artisan")) {
                userRole = "ROLE_FLORIST";
            } else if (request.getRole().equalsIgnoreCase("customer")) {
                userRole = "ROLE_BUYER";
            }
        }

        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(userRole)
                .build();
    }
}
