package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String status;
    private LocalDate deliveryDate;
    private String timeSlot;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String message;
}
