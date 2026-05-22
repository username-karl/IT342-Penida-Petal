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
public class CartItemResponse {
    private Long id;
    private Long productId;
    private Long floristId;
    private String productName;
    private String productImageUrl;
    private String floristName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
}
