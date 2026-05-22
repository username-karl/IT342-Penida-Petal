package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.SellerOrderResponse;
import com.petal.dto.SellerOrderStatusRequest;
import com.petal.entity.User;
import com.petal.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class FloristOrderAliasController {

    private final OrderService orderService;

    @GetMapping("/florist")
    public ResponseEntity<ApiResponse<List<SellerOrderResponse>>> getFloristOrders(
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(ApiResponse.<List<SellerOrderResponse>>builder()
                .success(true)
                .message("Seller orders fetched successfully")
                .data(orderService.getSellerOrders(seller))
                .build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SellerOrderResponse>> patchFloristOrderStatus(
            @AuthenticationPrincipal User seller,
            @PathVariable Long id,
            @Valid @RequestBody SellerOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<SellerOrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(orderService.updateSellerOrderStatus(seller, id, request))
                .build());
    }
}
