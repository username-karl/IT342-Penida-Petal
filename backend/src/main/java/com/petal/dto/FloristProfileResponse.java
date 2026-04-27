package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FloristProfileResponse {
    private Long id;
    private Long userId;
    private String storeName;
    private String bio;
    private String logoUrl;
    private String street;
    private String city;
    private String zipCode;
    private int maxDailyCapacity;
    private String ownerName;
    private String ownerEmail;
}
