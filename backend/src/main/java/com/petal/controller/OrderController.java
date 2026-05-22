package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.BuyerOrderResponse;
import com.petal.dto.CreateOrderRequest;
import com.petal.dto.OrderResponse;
import com.petal.dto.SellerOrderResponse;
import com.petal.entity.User;
import com.petal.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BuyerOrderResponse>>> getOrders(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<BuyerOrderResponse>>builder()
                .success(true)
                .message("Orders fetched successfully")
                .data(orderService.getBuyerOrders(user))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BuyerOrderResponse>> getOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<BuyerOrderResponse>builder()
                .success(true)
                .message("Order fetched successfully")
                .data(orderService.getBuyerOrder(user, id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order placed successfully")
                .data(orderService.createOrder(user, request))
                .build());
    }

    @PostMapping("/{id}/fulfillment-photo")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> uploadFulfillmentPhoto(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        requireFile(file);
        return ResponseEntity.ok(ApiResponse.<SellerOrderResponse>builder()
                .success(true)
                .message("Fulfillment photo uploaded successfully")
                .data(orderService.uploadFulfillmentPhoto(user, id, file))
                .build());
    }

    @PostMapping("/{id}/proof")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> uploadProofPhoto(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        requireFile(file);
        return ResponseEntity.ok(ApiResponse.<SellerOrderResponse>builder()
                .success(true)
                .message("Proof of delivery photo uploaded successfully")
                .data(orderService.uploadProofPhoto(user, id, file))
                .build());
    }

    private void requireFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }
    }
}
