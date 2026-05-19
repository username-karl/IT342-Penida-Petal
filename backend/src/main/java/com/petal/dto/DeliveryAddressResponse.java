package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressResponse {
    private Long id;
    private String label;
    private String recipientName;
    private String phoneNumber;
    private String addressLine;
    private boolean defaultAddress;
}
