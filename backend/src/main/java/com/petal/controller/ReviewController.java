package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.CreateReviewRequest;
import com.petal.dto.ReviewResponse;
import com.petal.dto.ReviewSummaryResponse;
import com.petal.entity.User;
import com.petal.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/orders/{orderId}/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request) {
        
        ReviewResponse response = reviewService.createReview(user.getId(), orderId, productId, request);
        
        return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                .success(true)
                .message("Review submitted successfully")
                .data(response)
                .build());
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ReviewSummaryResponse response = reviewService.getProductReviews(productId, page, size);
        
        return ResponseEntity.ok(ApiResponse.<ReviewSummaryResponse>builder()
                .success(true)
                .message("Product reviews fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/florists/{floristId}/reviews")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getFloristReviews(
            @PathVariable Long floristId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ReviewSummaryResponse response = reviewService.getFloristReviews(floristId, page, size);
        
        return ResponseEntity.ok(ApiResponse.<ReviewSummaryResponse>builder()
                .success(true)
                .message("Florist reviews fetched successfully")
                .data(response)
                .build());
    }
}
