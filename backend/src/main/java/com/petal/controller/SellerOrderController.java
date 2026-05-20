package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.SellerOrderResponse;
import com.petal.dto.SellerOrderStatusRequest;
import com.petal.dto.ShippingUpdateRequest;
import com.petal.entity.User;
import com.petal.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SellerOrderResponse>>> getSellerOrders(
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(ApiResponse.<List<SellerOrderResponse>>builder()
                .success(true)
                .message("Seller orders fetched successfully")
                .data(orderService.getSellerOrders(seller))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> getSellerOrder(
            @AuthenticationPrincipal User seller,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<SellerOrderResponse>builder()
                .success(true)
                .message("Seller order fetched successfully")
                .data(orderService.getSellerOrder(seller, id))
                .build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> updateSellerOrderStatus(
            @AuthenticationPrincipal User seller,
            @PathVariable Long id,
            @Valid @RequestBody SellerOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<SellerOrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(orderService.updateSellerOrderStatus(seller, id, request))
                .build());
    }

    @PutMapping("/{id}/shipping")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> updateSellerShipping(
            @AuthenticationPrincipal User seller,
            @PathVariable Long id,
            @Valid @RequestBody ShippingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<SellerOrderResponse>builder()
                .success(true)
                .message("Shipping status updated successfully")
                .data(orderService.updateSellerShipping(seller, id, request))
                .build());
    }
}
