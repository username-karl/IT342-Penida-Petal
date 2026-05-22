package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySlotAvailabilityResponse {
    private LocalDate date;
    private DeliverySlotResponse am;
    private DeliverySlotResponse pm;
}
