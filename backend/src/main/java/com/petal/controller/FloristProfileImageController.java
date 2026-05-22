package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.FloristResponse;
import com.petal.entity.User;
import com.petal.service.FloristService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/florists/profile")
@RequiredArgsConstructor
public class FloristProfileImageController {

    private final FloristService floristService;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<FloristResponse>> uploadProfileImage(
            @AuthenticationPrincipal User user,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.<FloristResponse>builder()
                .success(true)
                .message("Florist profile image uploaded successfully")
                .data(floristService.uploadProfileImage(user, file))
                .build());
    }
}
