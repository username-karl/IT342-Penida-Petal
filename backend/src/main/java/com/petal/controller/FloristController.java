package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.FloristProfileRequest;
import com.petal.dto.FloristProfileResponse;
import com.petal.entity.User;
import com.petal.service.FloristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/florists")
@RequiredArgsConstructor
public class FloristController {

    private final FloristService floristService;

    /**
     * GET /api/florists/profile
     * Get the current florist's profile (auto-creates if new).
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<FloristProfileResponse>> getProfile(
            @AuthenticationPrincipal User user) {
        FloristProfileResponse profile = floristService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.<FloristProfileResponse>builder()
                .success(true)
                .message("Florist profile fetched successfully")
                .data(profile)
                .build());
    }

    /**
     * PUT /api/florists/profile
     * Update the current florist's profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<FloristProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FloristProfileRequest request) {
        FloristProfileResponse profile = floristService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.<FloristProfileResponse>builder()
                .success(true)
                .message("Florist profile updated successfully")
                .data(profile)
                .build());
    }
}
