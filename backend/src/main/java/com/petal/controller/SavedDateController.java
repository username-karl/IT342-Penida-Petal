package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.SavedDateRequest;
import com.petal.dto.SavedDateResponse;
import com.petal.entity.User;
import com.petal.service.SavedDateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/dates")
@RequiredArgsConstructor
public class SavedDateController {

    private final SavedDateService savedDateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedDateResponse>>> getSavedDates(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<SavedDateResponse>>builder()
                .success(true)
                .message("Dates fetched successfully")
                .data(savedDateService.getSavedDates(user))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavedDateResponse>> createSavedDate(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SavedDateRequest request) {
        return ResponseEntity.ok(ApiResponse.<SavedDateResponse>builder()
                .success(true)
                .message("Date saved.")
                .data(savedDateService.createSavedDate(user, request))
                .build());
    }
}
