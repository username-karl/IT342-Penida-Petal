package com.petal.service;

import com.petal.dto.ConversationResponse;
import com.petal.dto.MessageResponse;
import com.petal.dto.SendMessageRequest;
import com.petal.entity.*;
import com.petal.exception.ForbiddenException;
import com.petal.repository.ConversationRepository;
import com.petal.repository.MessageRepository;
import com.petal.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final OrderRepository orderRepository;
    private final FloristService floristService;
    private final com.petal.repository.FloristRepository floristRepository;

    @Transactional
    public ConversationResponse createBuyerConversation(User buyer, Long orderId) {
        requireRole(buyer, "ROLE_BUYER");
        
        Order order = orderRepository.findByIdAndUser(orderId, buyer)
                .orElseThrow(() -> new ForbiddenException("Order not found or access denied"));

        return conversationRepository.findByOrderId(orderId)
                .map(this::toConversationResponse)
                .orElseGet(() -> {
                    Florist florist = getFloristFromOrder(order);
                    Conversation conversation = Conversation.builder()
                            .buyer(buyer)
                            .florist(florist)
                            .order(order)
                            .build();
                    Conversation saved = conversationRepository.save(conversation);
                    return toConversationResponse(saved);
                });
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getBuyerConversations(User buyer) {
        requireRole(buyer, "ROLE_BUYER");
        return conversationRepository.findByBuyerId(buyer.getId())
                .stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getFloristConversations(User floristUser) {
        Florist florist = requireFlorist(floristUser);
        return conversationRepository.findByFloristId(florist.getId())
                .stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(User user, Long conversationId) {
        Conversation conversation = getConversationAndVerifyAccess(user, conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(User user, Long conversationId, SendMessageRequest request) {
        Conversation conversation = getConversationAndVerifyAccess(user, conversationId);
        
        String senderType = "ROLE_BUYER".equals(user.getRole()) ? "BUYER" : "FLORIST";

        Message message = Message.builder()
                .conversation(conversation)
                .senderType(senderType)
                .content(request.getContent().trim())
                .build();

        Message saved = messageRepository.save(message);
        
        // update conversation updatedAt
        conversation.setUpdatedAt(saved.getCreatedAt());
        conversationRepository.save(conversation);

        return toMessageResponse(saved);
    }

    private Conversation getConversationAndVerifyAccess(User user, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if ("ROLE_BUYER".equals(user.getRole())) {
            if (!conversation.getBuyer().getId().equals(user.getId())) {
                throw new ForbiddenException("Access denied to this conversation");
            }
        } else if ("ROLE_FLORIST".equals(user.getRole())) {
            Florist florist = floristService.getOrCreateForUser(user);
            if (!conversation.getFlorist().getId().equals(florist.getId())) {
                throw new ForbiddenException("Access denied to this conversation");
            }
        } else {
            throw new ForbiddenException("Invalid role");
        }
        
        return conversation;
    }

    private void requireRole(User user, String expectedRole) {
        if (user == null || !expectedRole.equals(user.getRole())) {
            throw new ForbiddenException(expectedRole + " access is required");
        }
    }

    private Florist requireFlorist(User seller) {
        requireRole(seller, "ROLE_FLORIST");
        return floristService.getOrCreateForUser(seller);
    }

    private Florist getFloristFromOrder(Order order) {
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order has no items");
        }
        Long floristId = order.getItems().get(0).getProduct().getFloristId();
        return floristRepository.findById(floristId)
                .orElseThrow(() -> new IllegalArgumentException("Florist not found"));
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .buyerId(conversation.getBuyer().getId())
                .floristId(conversation.getFlorist().getId())
                .orderId(conversation.getOrder().getId())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
