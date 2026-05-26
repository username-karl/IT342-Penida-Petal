package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long productId;
    private String reviewerName;
    private Integer productRating;
    private Integer floristRating;
    private String comment;
    private LocalDateTime createdAt;
}
