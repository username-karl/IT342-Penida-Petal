package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private Long buyerId;
    private Long floristId;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
