package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.BuyerOrderResponse;
import com.petal.dto.CreateOrderRequest;
import com.petal.dto.OrderResponse;
import com.petal.dto.ShippingInfoResponse;
import com.petal.dto.TrackingEventResponse;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .paymentMethod("GCASH")
                .build();

        Mockito.when(orderService.createOrder(eq(user), eq(request))).thenReturn(OrderResponse.builder()
                .id(25L)
                .status("PENDING")
                .deliveryDate(request.getDeliveryDate())
                .timeSlot("AM")
                .paymentMethod("GCASH")
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
                .andExpect(jsonPath("$.data.paymentMethod", is("GCASH")))
                .andExpect(jsonPath("$.data.message", is("Order placed successfully.")));
    }

    @Test
    void getOrdersReturnsCurrentBuyerOrderHistory() throws Exception {
        User user = authenticatedUser();

        Mockito.when(orderService.getBuyerOrders(user)).thenReturn(List.of(
                BuyerOrderResponse.builder()
                        .id(25L)
                        .orderNumber("PET-0025")
                        .status("PREPARING")
                        .deliveryDate(LocalDate.of(2026, 5, 18))
                        .timeSlot("AM")
                        .paymentMethod("GCASH")
                        .totalAmount(new BigDecimal("2468.00"))
                        .itemSummary("Aurora Hydrangea x2")
                        .build()));

        mockMvc.perform(get("/api/orders")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderNumber", is("PET-0025")))
                .andExpect(jsonPath("$.data[0].status", is("PREPARING")))
                .andExpect(jsonPath("$.data[0].paymentMethod", is("GCASH")))
                .andExpect(jsonPath("$.data[0].itemSummary", is("Aurora Hydrangea x2")));
    }

    @Test
    void getOrderReturnsCurrentBuyerOrderDetail() throws Exception {
        User user = authenticatedUser();

        Mockito.when(orderService.getBuyerOrder(user, 25L)).thenReturn(
                BuyerOrderResponse.builder()
                        .id(25L)
                        .orderNumber("PET-0025")
                        .status("READY_FOR_PICKUP")
                        .deliveryDate(LocalDate.of(2026, 5, 18))
                        .timeSlot("PM")
                        .paymentMethod("MAYA")
                        .totalAmount(new BigDecimal("2468.00"))
                        .recipientName("Maria Santos")
                        .recipientAddress("Cebu Business Park")
                        .shipping(ShippingInfoResponse.builder()
                                .courierName("Petal Cebu Rider")
                                .trackingNumber("PETAL-TRACK-25")
                                .estimatedDeliveryDate(LocalDate.of(2026, 5, 19))
                                .latestStatus("Out for delivery")
                                .events(List.of(TrackingEventResponse.builder()
                                        .id(9L)
                                        .status("Out for delivery")
                                        .description("Your bouquet is on the way to the recipient.")
                                        .timestamp(LocalDateTime.of(2026, 5, 19, 12, 10))
                                        .build()))
                                .build())
                        .itemSummary("Aurora Hydrangea x2")
                        .build());

        mockMvc.perform(get("/api/orders/25")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderNumber", is("PET-0025")))
                .andExpect(jsonPath("$.data.status", is("READY_FOR_PICKUP")))
                .andExpect(jsonPath("$.data.paymentMethod", is("MAYA")))
                .andExpect(jsonPath("$.data.recipientAddress", is("Cebu Business Park")))
                .andExpect(jsonPath("$.data.shipping.courierName", is("Petal Cebu Rider")))
                .andExpect(jsonPath("$.data.shipping.trackingNumber", is("PETAL-TRACK-25")))
                .andExpect(jsonPath("$.data.shipping.events[0].status", is("Out for delivery")));
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
