package com.petal.controller;

import com.petal.dto.AddCartItemRequest;
import com.petal.dto.ApiResponse;
import com.petal.dto.CartItemResponse;
import com.petal.dto.CartResponse;
import com.petal.dto.UpdateCartItemRequest;
import com.petal.entity.User;
import com.petal.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart fetched successfully")
                .data(cartService.getCart(user))
                .build());
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.<CartItemResponse>builder()
                .success(true)
                .message("Item added to cart")
                .data(cartService.addItem(user, request.getProductId(), request.getQuantity()))
                .build());
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.<CartItemResponse>builder()
                .success(true)
                .message("Cart item updated")
                .data(cartService.updateItem(user, id, request.getQuantity()))
                .build());
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        cartService.removeItem(user, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Cart item removed")
                .build());
    }
}
