package com.petal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FloristProfileRequest {

    @NotBlank(message = "Store name is required")
    private String storeName;

    private String bio;
    private String street;
    private String city;
    private String zipCode;
}
