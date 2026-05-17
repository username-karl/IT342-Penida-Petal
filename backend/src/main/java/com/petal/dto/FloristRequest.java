package com.petal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloristRequest {
    @Size(max = 255, message = "Store name must be 255 characters or fewer")
    private String storeName;

    @Size(max = 1000, message = "Bio must be 1000 characters or fewer")
    private String bio;

    private String logoUrl;
    private String street;
    private String city;
    private String zipCode;

    @Min(value = 1, message = "Daily capacity must be at least 1")
    private Integer maxDailyCapacity;

    private String deliveryCoverage;
    private String timeSlots;

    @Min(value = 1, message = "Preparation lead time must be at least 1 hour")
    private Integer prepLeadTimeHours;

    private Boolean onboardingComplete;
}
