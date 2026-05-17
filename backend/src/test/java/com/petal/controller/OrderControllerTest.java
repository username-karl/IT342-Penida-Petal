package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.CreateOrderRequest;
import com.petal.dto.OrderResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.OrderService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createOrderPlacesPendingOrderForCurrentUsersCart() throws Exception {
        User user = authenticatedUser();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .recipientName("Maria Santos")
                .recipientAddress("Cebu Business Park, Cebu City")
                .cardMessage("Happy birthday!")
                .deliveryDate(LocalDate.now().plusDays(1))
                .timeSlot("AM")
                .build();

        Mockito.when(orderService.createOrder(eq(user), eq(request))).thenReturn(OrderResponse.builder()
                .id(25L)
                .status("PENDING")
                .deliveryDate(request.getDeliveryDate())
                .timeSlot("AM")
                .totalAmount(new BigDecimal("98.00"))
                .message("Order placed successfully.")
                .build());

        mockMvc.perform(post("/api/orders")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.timeSlot", is("AM")))
                .andExpect(jsonPath("$.data.message", is("Order placed successfully.")));
    }

    private User authenticatedUser() {
        User user = User.builder()
                .id(1L)
                .name("Karl")
                .email("karl@example.com")
                .role("ROLE_BUYER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }
}
