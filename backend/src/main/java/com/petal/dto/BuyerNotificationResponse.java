package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerNotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long savedDateId;
    private LocalDate eventDate;
    private Integer notificationYear;
    private boolean read;
    private Instant createdAt;
    private Instant readAt;
}
