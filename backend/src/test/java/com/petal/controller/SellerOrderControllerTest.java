package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.SellerOrderResponse;
import com.petal.dto.ShippingInfoResponse;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class SellerOrderControllerTest {

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
    void getSellerOrdersReturnsAuthenticatedFloristOrders() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(orderService.getSellerOrders(seller)).thenReturn(List.of(
                SellerOrderResponse.builder()
                        .id(12L)
                        .orderNumber("PET-0012")
                        .buyerName("Mikaela Santos")
                        .recipientName("Lara Santos")
                        .recipientAddress("Cebu Business Park")
                        .deliveryDate(LocalDate.of(2026, 5, 18))
                        .timeSlot("AM")
                        .status("PENDING")
                        .paymentMethod("GCASH")
                        .sellerSubtotal(new BigDecimal("2950.00"))
                        .itemSummary("Aurora Hydrangea x2")
                        .build()));

        mockMvc.perform(get("/api/seller/orders")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderNumber", is("PET-0012")))
                .andExpect(jsonPath("$.data[0].paymentMethod", is("GCASH")))
                .andExpect(jsonPath("$.data[0].sellerSubtotal", is(2950.00)))
                .andExpect(jsonPath("$.data[0].itemSummary", is("Aurora Hydrangea x2")));
    }

    @Test
    void updateSellerOrderStatusReturnsUpdatedOrder() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(orderService.updateSellerOrderStatus(eq(seller), eq(12L), any())).thenReturn(
                SellerOrderResponse.builder()
                        .id(12L)
                        .orderNumber("PET-0012")
                        .status("ACCEPTED")
                        .paymentMethod("GCASH")
                        .sellerSubtotal(new BigDecimal("2950.00"))
                        .build());

        mockMvc.perform(put("/api/seller/orders/12/status")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("status", "ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderNumber", is("PET-0012")))
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")));
    }

    @Test
    void getSellerOrderReturnsSellerOwnedOrderDetail() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(orderService.getSellerOrder(seller, 12L)).thenReturn(
                SellerOrderResponse.builder()
                        .id(12L)
                        .orderNumber("PET-0012")
                        .buyerName("Mikaela Santos")
                        .recipientName("Lara Santos")
                        .recipientAddress("Cebu Business Park")
                        .deliveryDate(LocalDate.of(2026, 5, 18))
                        .timeSlot("AM")
                        .status("PENDING")
                        .paymentMethod("GCASH")
                        .sellerSubtotal(new BigDecimal("2950.00"))
                        .itemSummary("Aurora Hydrangea x2")
                        .build());

        mockMvc.perform(get("/api/seller/orders/12")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.orderNumber", is("PET-0012")))
                .andExpect(jsonPath("$.data.buyerName", is("Mikaela Santos")))
                .andExpect(jsonPath("$.data.paymentMethod", is("GCASH")))
                .andExpect(jsonPath("$.data.recipientAddress", is("Cebu Business Park")));
    }

    @Test
    void updateSellerShippingStatusAddsTrackingEvent() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(orderService.updateSellerShipping(eq(seller), eq(12L), any())).thenReturn(
                SellerOrderResponse.builder()
                        .id(12L)
                        .orderNumber("PET-0012")
                        .status("OUT_FOR_DELIVERY")
                        .shipping(ShippingInfoResponse.builder()
                                .courierName("Petal Cebu Rider")
                                .trackingNumber("PETAL-0012-RIDER")
                                .latestStatus("Out for delivery")
                                .build())
                        .build());

        mockMvc.perform(put("/api/seller/orders/12/shipping")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "courierName", "Petal Cebu Rider",
                                "trackingNumber", "PETAL-0012-RIDER",
                                "deliveryStatus", "OUT_FOR_DELIVERY",
                                "trackingMessage", "Your bouquet is on the way to the recipient.",
                                "timestamp", LocalDateTime.of(2026, 5, 19, 12, 10).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("OUT_FOR_DELIVERY")))
                .andExpect(jsonPath("$.data.shipping.latestStatus", is("Out for delivery")));
    }

    private User authenticatedSeller() {
        User user = User.builder()
                .id(4L)
                .name("Karl")
                .email("karl@petal.test")
                .password("encoded")
                .role("ROLE_FLORIST")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_FLORIST"))));

        return user;
    }
}
