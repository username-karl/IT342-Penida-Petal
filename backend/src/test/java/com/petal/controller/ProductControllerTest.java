package com.petal.controller;

import com.petal.dto.ProductResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.ProductService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProductController.class, SellerProductController.class})
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
                        .floristLogoUrl("/uploads/florist-logos/petal-house.png")
                        .floristBio("Thoughtful Cebu arrangements.")
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
                .andExpect(jsonPath("$.data[0].floristLogoUrl", is("/uploads/florist-logos/petal-house.png")))
                .andExpect(jsonPath("$.data[0].floristBio", is("Thoughtful Cebu arrangements.")))
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

    @Test
    void getSellerProductsReturnsAuthenticatedFloristProducts() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(productService.getSellerProducts(seller)).thenReturn(List.of(
                ProductResponse.builder()
                        .id(7L)
                        .name("Cebu Sun Basket")
                        .description("Warm preserved flowers for Cebu deliveries")
                        .price(new BigDecimal("1850.00"))
                        .moodTags(List.of("celebration"))
                        .imageUrl("/images/cebu-sun.webp")
                        .floristId(4L)
                        .floristName("Karl's Studio")
                        .inStock(true)
                        .build()));

        mockMvc.perform(get("/api/seller/products")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].floristName", is("Karl's Studio")));
    }

    @Test
    void createProductReturnsCreatedSellerProduct() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(productService.createSellerProduct(eq(seller), any())).thenReturn(
                ProductResponse.builder()
                        .id(9L)
                        .name("Island Blush")
                        .description("Pink preserved flowers")
                        .price(new BigDecimal("2100.00"))
                        .moodTags(List.of("romance", "celebration"))
                        .imageUrl("/images/island-blush.webp")
                        .floristId(4L)
                        .floristName("Karl's Studio")
                        .inStock(true)
                        .build());

        mockMvc.perform(post("/api/seller/products")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Island Blush",
                                  "description": "Pink preserved flowers",
                                  "price": 2100,
                                  "moodTags": ["romance", "celebration"],
                                  "imageUrl": "/images/island-blush.webp",
                                  "floristName": "Karl's Studio",
                                  "inStock": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(9)))
                .andExpect(jsonPath("$.data.name", is("Island Blush")));
    }

    @Test
    void updateProductReturnsUpdatedSellerProduct() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(productService.updateSellerProduct(eq(seller), eq(9L), any())).thenReturn(
                ProductResponse.builder()
                        .id(9L)
                        .name("Island Blush Updated")
                        .description("Updated pink preserved flowers")
                        .price(new BigDecimal("2100.00"))
                        .moodTags(List.of("romance", "celebration"))
                        .imageUrl("/images/island-blush.webp")
                        .floristId(4L)
                        .floristName("Karl's Studio")
                        .inStock(false)
                        .build());

        mockMvc.perform(put("/api/seller/products/9")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Island Blush Updated",
                                  "description": "Updated pink preserved flowers",
                                  "price": 2100,
                                  "moodTags": ["romance", "celebration"],
                                  "imageUrl": "/images/island-blush.webp",
                                  "floristName": "Karl's Studio",
                                  "inStock": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(9)))
                .andExpect(jsonPath("$.data.inStock", is(false)));
    }


    @Test
    void deleteProductReturnsSuccessMessage() throws Exception {
        User seller = authenticatedSeller();

        mockMvc.perform(delete("/api/seller/products/9")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Product deleted successfully")));

        Mockito.verify(productService).deleteSellerProduct(seller, 9L);
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
