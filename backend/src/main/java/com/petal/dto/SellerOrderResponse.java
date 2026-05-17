package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderResponse {
    private Long id;
    private String orderNumber;
    private String buyerName;
    private String recipientName;
    private String recipientAddress;
    private String cardMessage;
    private LocalDate deliveryDate;
    private String timeSlot;
    private String status;
    private BigDecimal sellerSubtotal;
    private String itemSummary;
    private List<SellerOrderItemResponse> items;
}
