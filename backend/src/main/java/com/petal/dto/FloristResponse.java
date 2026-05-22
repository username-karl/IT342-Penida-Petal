package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloristResponse {
    private Long id;
    private Long userId;
    private String storeName;
    private String bio;
    private String logoUrl;
    private String street;
    private String city;
    private String zipCode;
    private Integer maxDailyCapacity;
    private String deliveryCoverage;
    private String timeSlots;
    private Integer prepLeadTimeHours;
    private boolean onboardingComplete;
}
