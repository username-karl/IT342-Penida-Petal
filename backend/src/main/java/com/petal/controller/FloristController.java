package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.FloristRequest;
import com.petal.dto.FloristResponse;
import com.petal.entity.User;
import com.petal.service.FloristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/seller/florist")
@RequiredArgsConstructor
public class FloristController {

    private final FloristService floristService;

    @GetMapping
    public ResponseEntity<ApiResponse<FloristResponse>> getCurrentFlorist(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<FloristResponse>builder()
                .success(true)
                .message("Florist profile fetched successfully")
                .data(floristService.getCurrentFlorist(user))
                .build());
    }

    @PutMapping
    public ResponseEntity<ApiResponse<FloristResponse>> updateCurrentFlorist(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FloristRequest request) {
        return ResponseEntity.ok(ApiResponse.<FloristResponse>builder()
                .success(true)
                .message("Florist profile updated successfully")
                .data(floristService.updateCurrentFlorist(user, request))
                .build());
    }

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<FloristResponse>> uploadCurrentFloristImage(
            @AuthenticationPrincipal User user,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.<FloristResponse>builder()
                .success(true)
                .message("Florist profile image uploaded successfully")
                .data(floristService.uploadProfileImage(user, file))
                .build());
    }
}
