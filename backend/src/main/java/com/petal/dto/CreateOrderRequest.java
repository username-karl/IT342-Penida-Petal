package com.petal.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @NotBlank(message = "Recipient address is required")
    private String recipientAddress;

    @Size(max = 200, message = "Card message must be 200 characters or fewer")
    private String cardMessage;

    @NotNull(message = "Delivery date is required")
    @FutureOrPresent(message = "Delivery date cannot be in the past")
    private LocalDate deliveryDate;

    @NotBlank(message = "Time slot is required")
    private String timeSlot;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
