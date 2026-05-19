package com.petal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressRequest {

    @NotBlank(message = "Address label is required")
    @Size(max = 80, message = "Address label must be 80 characters or fewer")
    private String label;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 120, message = "Recipient name must be 120 characters or fewer")
    private String recipientName;

    @Size(max = 40, message = "Phone number must be 40 characters or fewer")
    private String phoneNumber;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 500, message = "Delivery address must be 500 characters or fewer")
    private String addressLine;

    private boolean defaultAddress;
}
