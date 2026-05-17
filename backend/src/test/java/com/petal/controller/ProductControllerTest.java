package com.petal.controller;

import com.petal.dto.ProductResponse;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getProductsReturnsAllAvailableProducts() throws Exception {
        Mockito.when(productService.getProducts(null)).thenReturn(List.of(
                ProductResponse.builder()
                        .id(1L)
                        .name("Rose Reverie")
                        .description("Soft red rose arrangement")
                        .price(new BigDecimal("1499.00"))
                        .moodTags(List.of("romance", "celebration"))
                        .imageUrl("/images/rose-reverie.webp")
                        .floristName("Petal House")
                        .inStock(true)
                        .build(),
                ProductResponse.builder()
                        .id(2L)
                        .name("Gentle Apology")
                        .description("White lilies and baby's breath")
                        .price(new BigDecimal("1299.00"))
                        .moodTags(List.of("apology", "sympathy"))
                        .imageUrl("/images/gentle-apology.webp")
                        .floristName("Petal House")
                        .inStock(true)
                        .build()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("Rose Reverie")))
                .andExpect(jsonPath("$.data[1].moodTags[0]", is("apology")));
    }

    @Test
    void getProductsFiltersByMoodIgnoringCase() throws Exception {
        Mockito.when(productService.getProducts("sympathy")).thenReturn(List.of(
                ProductResponse.builder()
                        .id(2L)
                        .name("Gentle Apology")
                        .description("White lilies and baby's breath")
                        .price(new BigDecimal("1299.00"))
                        .moodTags(List.of("apology", "sympathy"))
                        .imageUrl("/images/gentle-apology.webp")
                        .floristName("Petal House")
                        .inStock(true)
                        .build()));

        mockMvc.perform(get("/api/products").param("mood", "sympathy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Gentle Apology")))
                .andExpect(jsonPath("$.data[0].moodTags[1]", is("sympathy")));

        Mockito.verify(productService).getProducts(eq("sympathy"));
    }

    @Test
    void getProductByIdReturnsSingleProduct() throws Exception {
        Mockito.when(productService.getProductById(1L)).thenReturn(
                ProductResponse.builder()
                        .id(1L)
                        .name("Rose Reverie")
                        .description("Soft red rose arrangement")
                        .price(new BigDecimal("1499.00"))
                        .moodTags(List.of("romance", "celebration"))
                        .imageUrl("/images/rose-reverie.webp")
                        .floristName("Petal House")
                        .inStock(true)
                        .build());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("Rose Reverie")));
    }
}
