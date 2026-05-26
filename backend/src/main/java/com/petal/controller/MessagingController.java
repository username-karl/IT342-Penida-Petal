package com.petal.controller;

import com.petal.dto.ApiResponse;
import com.petal.dto.ConversationResponse;
import com.petal.dto.MessageResponse;
import com.petal.dto.SendMessageRequest;
import com.petal.entity.User;
import com.petal.service.MessagingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    // Buyer endpoints

    @PostMapping("/api/buyer/orders/{orderId}/conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @AuthenticationPrincipal User buyer,
            @PathVariable Long orderId) {
        ConversationResponse response = messagingService.createBuyerConversation(buyer, orderId);
        return ResponseEntity.ok(ApiResponse.<ConversationResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/api/buyer/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getBuyerConversations(
            @AuthenticationPrincipal User buyer) {
        List<ConversationResponse> response = messagingService.getBuyerConversations(buyer);
        return ResponseEntity.ok(ApiResponse.<List<ConversationResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/api/buyer/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getBuyerMessages(
            @AuthenticationPrincipal User buyer,
            @PathVariable Long conversationId) {
        List<MessageResponse> response = messagingService.getConversationMessages(buyer, conversationId);
        return ResponseEntity.ok(ApiResponse.<List<MessageResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/api/buyer/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendBuyerMessage(
            @AuthenticationPrincipal User buyer,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messagingService.sendMessage(buyer, conversationId, request);
        return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    // Florist (Seller) endpoints

    @GetMapping("/api/seller/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getSellerConversations(
            @AuthenticationPrincipal User seller) {
        List<ConversationResponse> response = messagingService.getFloristConversations(seller);
        return ResponseEntity.ok(ApiResponse.<List<ConversationResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/api/seller/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getSellerMessages(
            @AuthenticationPrincipal User seller,
            @PathVariable Long conversationId) {
        List<MessageResponse> response = messagingService.getConversationMessages(seller, conversationId);
        return ResponseEntity.ok(ApiResponse.<List<MessageResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/api/seller/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendSellerMessage(
            @AuthenticationPrincipal User seller,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messagingService.sendMessage(seller, conversationId, request);
        return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                .success(true)
                .data(response)
                .build());
    }
}
