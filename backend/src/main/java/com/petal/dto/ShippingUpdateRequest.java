package com.petal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingUpdateRequest {

    @jakarta.validation.constraints.NotBlank(message = "Courier name is required")
    private String courierName;

    @jakarta.validation.constraints.NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    private LocalDate estimatedDeliveryDate;

    @jakarta.validation.constraints.NotBlank(message = "Delivery status is required")
    private String deliveryStatus;

    @Size(max = 500, message = "Tracking message must be 500 characters or fewer")
    private String trackingMessage;

    @NotNull(message = "Tracking date/time is required")
    @PastOrPresent(message = "Tracking date/time cannot be in the future")
    private LocalDateTime timestamp;
}
