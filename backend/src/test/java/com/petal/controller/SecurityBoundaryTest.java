package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.config.SecurityConfig;
import com.petal.dto.CartResponse;
import com.petal.dto.DeliveryAddressResponse;
import com.petal.dto.FloristResponse;
import com.petal.dto.SellerOrderResponse;
import com.petal.entity.User;
import com.petal.exception.GlobalExceptionHandler;
import com.petal.repository.UserRepository;
import com.petal.security.JwtFilter;
import com.petal.security.JwtUtil;
import com.petal.service.CartService;
import com.petal.service.BuyerNotificationService;
import com.petal.service.DeliveryAddressService;
import com.petal.service.FloristService;
import com.petal.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuthController.class,
        CartController.class,
        DeliveryAddressController.class,
        FloristController.class,
        FloristProfileImageController.class,
        BuyerNotificationController.class,
        OrderController.class,
        UserAddressController.class,
        SellerOrderController.class
})
@Import({SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class})
class SecurityBoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CartService cartService;

    @MockBean
    private BuyerNotificationService buyerNotificationService;

    @MockBean
    private DeliveryAddressService deliveryAddressService;

    @MockBean
    private FloristService floristService;

    @MockBean
    private OrderService orderService;

    @Test
    void buyerEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyerEndpointWithFloristTokenReturnsForbidden() throws Exception {
        authenticateToken("florist-token", florist());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer florist-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void floristEndpointWithBuyerTokenReturnsForbidden() throws Exception {
        authenticateToken("buyer-token", buyer());

        mockMvc.perform(get("/api/seller/florist")
                        .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void floristLogoUploadWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(multipart("/api/florists/profile/image")
                        .file("file", "image-bytes".getBytes()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void floristLogoUploadWithBuyerTokenReturnsForbidden() throws Exception {
        User buyer = buyer();
        authenticateToken("buyer-token", buyer);

        mockMvc.perform(multipart("/api/florists/profile/image")
                        .file("file", "image-bytes".getBytes())
                        .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sddFloristProfileUpdateWithBuyerTokenReturnsForbidden() throws Exception {
        authenticateToken("buyer-token", buyer());

        mockMvc.perform(put("/api/florists/profile")
                        .header("Authorization", "Bearer buyer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "storeName", "Buyer Studio",
                                "bio", "Nope",
                                "city", "Cebu",
                                "maxDailyCapacity", 4))))
                .andExpect(status().isForbidden());
    }

    @Test
    void sddFloristOrdersWithBuyerTokenReturnsForbidden() throws Exception {
        authenticateToken("buyer-token", buyer());

        mockMvc.perform(get("/api/orders/florist")
                        .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sddOrderStatusPatchWithBuyerTokenReturnsForbidden() throws Exception {
        authenticateToken("buyer-token", buyer());

        mockMvc.perform(patch("/api/orders/12/status")
                        .header("Authorization", "Bearer buyer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTED"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void sddUserAddressPostWithFloristTokenReturnsForbidden() throws Exception {
        authenticateToken("florist-token", florist());

        mockMvc.perform(post("/api/users/addresses")
                        .header("Authorization", "Bearer florist-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "label", "Studio",
                                "recipientName", "Mika",
                                "phoneNumber", "09171234567",
                                "addressLine", "Cebu",
                                "defaultAddress", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerNotificationsWithFloristTokenReturnsForbidden() throws Exception {
        authenticateToken("florist-token", florist());

        mockMvc.perform(get("/api/users/notifications")
                        .header("Authorization", "Bearer florist-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerNotificationsWithBuyerTokenReturnsNotifications() throws Exception {
        User buyer = buyer();
        authenticateToken("buyer-token", buyer);
        Mockito.when(buyerNotificationService.getNotifications(buyer, false)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/notifications")
                        .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void sddUserAddressGetWithBuyerTokenReturnsAddresses() throws Exception {
        User buyer = buyer();
        authenticateToken("buyer-token", buyer);
        Mockito.when(deliveryAddressService.getAddresses(buyer)).thenReturn(List.of(
                DeliveryAddressResponse.builder()
                        .id(5L)
                        .label("Home")
                        .recipientName("Mika Santos")
                        .phoneNumber("09171234567")
                        .addressLine("Cebu Business Park")
                        .defaultAddress(true)
                        .build()));

        mockMvc.perform(get("/api/users/addresses")
                        .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].recipientName", is("Mika Santos")));
    }

    @Test
    void validBuyerCanAccessBuyerEndpoint() throws Exception {
        User buyer = buyer();
        authenticateToken("buyer-token", buyer);
        Mockito.when(cartService.getCart(buyer)).thenReturn(CartResponse.builder()
                .items(List.of())
                .build());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer buyer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void validFloristCanAccessFloristEndpoint() throws Exception {
        User florist = florist();
        authenticateToken("florist-token", florist);
        Mockito.when(floristService.getCurrentFlorist(florist)).thenReturn(FloristResponse.builder()
                .id(7L)
                .storeName("Petal Studio")
                .build());

        mockMvc.perform(get("/api/seller/florist")
                        .header("Authorization", "Bearer florist-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeName", is("Petal Studio")));
    }

    @Test
    void sellerOrderOwnerMismatchReturnsForbidden() throws Exception {
        User florist = florist();
        authenticateToken("florist-token", florist);
        Mockito.when(orderService.getSellerOrder(florist, 99L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Order not found for this seller"));

        mockMvc.perform(get("/api/seller/orders/99")
                        .header("Authorization", "Bearer florist-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void registrationRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Karl",
                                "email", "karl@example.com",
                                "password", "password",
                                "role", "customer"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    private void authenticateToken(String token, User user) {
        Mockito.when(jwtUtil.isTokenValid(token)).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail(token)).thenReturn(user.getEmail());
        Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    private User buyer() {
        return User.builder()
                .id(1L)
                .name("Buyer")
                .email("buyer@petal.test")
                .password("encoded")
                .role("ROLE_BUYER")
                .build();
    }

    private User florist() {
        return User.builder()
                .id(2L)
                .name("Florist")
                .email("florist@petal.test")
                .password("encoded")
                .role("ROLE_FLORIST")
                .build();
    }
}
