package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.DeliveryAddressRequest;
import com.petal.dto.DeliveryAddressResponse;
import com.petal.entity.User;
import com.petal.service.DeliveryAddressService;
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

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryAddressResponse>>> getAddresses(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<DeliveryAddressResponse>>builder()
                .success(true)
                .message("Addresses fetched successfully")
                .data(deliveryAddressService.getAddresses(user))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryAddressResponse>> createAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.<DeliveryAddressResponse>builder()
                .success(true)
                .message("Address saved successfully")
                .data(deliveryAddressService.createAddress(user, request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryAddressResponse>> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.<DeliveryAddressResponse>builder()
                .success(true)
                .message("Address updated successfully")
                .data(deliveryAddressService.updateAddress(user, id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        deliveryAddressService.deleteAddress(user, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Address removed")
                .build());
    }
}
