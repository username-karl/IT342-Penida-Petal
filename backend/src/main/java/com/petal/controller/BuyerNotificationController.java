package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.BuyerNotificationResponse;
import com.petal.entity.User;
import com.petal.service.BuyerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/notifications")
@RequiredArgsConstructor
public class BuyerNotificationController {

    private final BuyerNotificationService buyerNotificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BuyerNotificationResponse>>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(ApiResponse.<List<BuyerNotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched successfully")
                .data(buyerNotificationService.getNotifications(user, unreadOnly))
                .build());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<BuyerNotificationResponse>> markRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<BuyerNotificationResponse>builder()
                .success(true)
                .message("Notification marked as read")
                .data(buyerNotificationService.markRead(user, id))
                .build());
    }
}
