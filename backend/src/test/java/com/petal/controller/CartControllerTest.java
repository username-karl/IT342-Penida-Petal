package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.AddCartItemRequest;
import com.petal.dto.CartItemResponse;
import com.petal.dto.CartResponse;
import com.petal.dto.UpdateCartItemRequest;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.CartService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getCartReturnsCurrentUsersCart() throws Exception {
        User user = authenticatedUser();
        CartResponse cart = CartResponse.builder()
                .items(List.of(CartItemResponse.builder()
                        .id(10L)
                        .productId(7L)
                        .productName("Kanso Vase")
                        .productImageUrl("/images/product_ceramic_vase_1771726567287.png")
                        .floristName("Ceramics by Jo")
                        .unitPrice(new BigDecimal("55.00"))
                        .quantity(2)
                        .lineTotal(new BigDecimal("110.00"))
                        .build()))
                .subtotal(new BigDecimal("110.00"))
                .build();

        Mockito.when(cartService.getCart(user)).thenReturn(cart);

        mockMvc.perform(get("/api/cart").principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].productName", is("Kanso Vase")))
                .andExpect(jsonPath("$.data.subtotal", is(110.00)));
    }

    @Test
    void addCartItemAddsProductForCurrentUser() throws Exception {
        User user = authenticatedUser();
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(7L)
                .quantity(2)
                .build();

        Mockito.when(cartService.addItem(eq(user), eq(7L), eq(2))).thenReturn(CartItemResponse.builder()
                .id(10L)
                .productId(7L)
                .productName("Kanso Vase")
                .unitPrice(new BigDecimal("55.00"))
                .quantity(2)
                .lineTotal(new BigDecimal("110.00"))
                .build());

        mockMvc.perform(post("/api/cart/items")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.quantity", is(2)));
    }

    @Test
    void updateCartItemUpdatesQuantityForCurrentUser() throws Exception {
        User user = authenticatedUser();
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(3)
                .build();

        Mockito.when(cartService.updateItem(eq(user), eq(10L), eq(3))).thenReturn(CartItemResponse.builder()
                .id(10L)
                .productId(7L)
                .productName("Kanso Vase")
                .unitPrice(new BigDecimal("55.00"))
                .quantity(3)
                .lineTotal(new BigDecimal("165.00"))
                .build());

        mockMvc.perform(put("/api/cart/items/10")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.lineTotal", is(165.00)));
    }

    @Test
    void deleteCartItemRemovesItemForCurrentUser() throws Exception {
        User user = authenticatedUser();

        mockMvc.perform(delete("/api/cart/items/10")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        Mockito.verify(cartService).removeItem(user, 10L);
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
