package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerOrderItemResponse {
    private Long productId;
    private String productName;
    private String imageUrl;
    private String floristName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
