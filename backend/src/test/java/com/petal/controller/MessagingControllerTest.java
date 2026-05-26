package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.ConversationResponse;
import com.petal.dto.MessageResponse;
import com.petal.dto.SendMessageRequest;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.MessagingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessagingController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessagingService messagingService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createConversationSuccess() throws Exception {
        User user = authenticatedUser("ROLE_BUYER");
        
        Mockito.when(messagingService.createBuyerConversation(eq(user), eq(1L)))
                .thenReturn(ConversationResponse.builder().id(10L).orderId(1L).build());

        mockMvc.perform(post("/api/buyer/orders/1/conversation")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(10)));
    }

    @Test
    void createConversationCrossBuyerFails() throws Exception {
        User user = authenticatedUser("ROLE_BUYER");
        
        Mockito.when(messagingService.createBuyerConversation(eq(user), eq(1L)))
                .thenThrow(new ForbiddenException("Order not found or access denied"));

        mockMvc.perform(post("/api/buyer/orders/1/conversation")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBuyerConversationsSuccess() throws Exception {
        User user = authenticatedUser("ROLE_BUYER");

        Mockito.when(messagingService.getBuyerConversations(eq(user)))
                .thenReturn(List.of(ConversationResponse.builder().id(10L).orderId(1L).build()));

        mockMvc.perform(get("/api/buyer/conversations")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].id", is(10)));
    }

    @Test
    void sendBuyerMessageEmptyContentFails() throws Exception {
        authenticatedUser("ROLE_BUYER");
        SendMessageRequest request = SendMessageRequest.builder().content("").build();

        mockMvc.perform(post("/api/buyer/conversations/10/messages")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendBuyerMessageSuccess() throws Exception {
        User user = authenticatedUser("ROLE_BUYER");
        SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();

        Mockito.when(messagingService.sendMessage(eq(user), eq(10L), any()))
                .thenReturn(MessageResponse.builder().id(100L).content("Hello").build());

        mockMvc.perform(post("/api/buyer/conversations/10/messages")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", is("Hello")));
    }

    @Test
    void getSellerConversationsSuccess() throws Exception {
        User seller = authenticatedUser("ROLE_FLORIST");

        Mockito.when(messagingService.getFloristConversations(eq(seller)))
                .thenReturn(List.of(ConversationResponse.builder().id(20L).orderId(2L).build()));

        mockMvc.perform(get("/api/seller/conversations")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].id", is(20)));
    }

    @Test
    void sendSellerMessageCrossFloristFails() throws Exception {
        User seller = authenticatedUser("ROLE_FLORIST");
        SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();

        Mockito.when(messagingService.sendMessage(eq(seller), eq(20L), any()))
                .thenThrow(new ForbiddenException("Access denied to this conversation"));

        mockMvc.perform(post("/api/seller/conversations/20/messages")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private User authenticatedUser(String role) {
        User user = User.builder()
                .id(1L)
                .name("Karl")
                .email("karl@example.com")
                .role(role)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }
}
