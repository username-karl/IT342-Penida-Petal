package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.DeliverySlotAvailabilityResponse;
import com.petal.entity.User;
import com.petal.service.DeliverySlotAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class DeliverySlotController {

    private final DeliverySlotAvailabilityService deliverySlotAvailabilityService;

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<DeliverySlotAvailabilityResponse>> getAvailability(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "florist_id", required = false) Long floristId,
            @RequestParam(value = "floristId", required = false) Long camelCaseFloristId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long resolvedFloristId = floristId != null ? floristId : camelCaseFloristId;
        if (resolvedFloristId == null) {
            throw new IllegalArgumentException("Florist id is required");
        }
        return ResponseEntity.ok(ApiResponse.<DeliverySlotAvailabilityResponse>builder()
                .success(true)
                .message("Delivery slot availability fetched successfully")
                .data(deliverySlotAvailabilityService.getAvailability(user, resolvedFloristId, date))
                .build());
    }
}
