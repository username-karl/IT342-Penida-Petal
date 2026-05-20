package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfoResponse {
    private String courierName;
    private String trackingNumber;
    private LocalDate estimatedDeliveryDate;
    private String latestStatus;
    private List<TrackingEventResponse> events;
}
