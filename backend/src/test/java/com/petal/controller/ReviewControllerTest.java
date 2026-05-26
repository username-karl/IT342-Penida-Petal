package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.CreateReviewRequest;
import com.petal.dto.ReviewResponse;
import com.petal.dto.ReviewSummaryResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createReviewSuccess() throws Exception {
        User user = authenticatedUser();
        CreateReviewRequest request = CreateReviewRequest.builder()
                .productRating(5)
                .floristRating(4)
                .comment("Great!")
                .build();

        Mockito.when(reviewService.createReview(eq(user.getId()), eq(1L), eq(2L), any())).thenReturn(
                ReviewResponse.builder()
                        .productRating(5)
                        .floristRating(4)
                        .reviewerName("Karl")
                        .build()
        );

        mockMvc.perform(post("/api/orders/1/products/2/reviews")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.productRating", is(5)));
    }

    @Test
    void createReviewFailsValidation() throws Exception {
        authenticatedUser();
        CreateReviewRequest request = CreateReviewRequest.builder()
                .productRating(6)
                .floristRating(4)
                .build();

        mockMvc.perform(post("/api/orders/1/products/2/reviews")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void getProductReviewsPublicly() throws Exception {
        Mockito.when(reviewService.getProductReviews(eq(2L), any(Integer.class), any(Integer.class))).thenReturn(
                ReviewSummaryResponse.builder()
                        .averageRating(4.5)
                        .totalReviews(2L)
                        .recentReviews(List.of())
                        .build()
        );

        mockMvc.perform(get("/api/products/2/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalReviews", is(2)));
    }

    @Test
    void createReviewConflict() throws Exception {
        User user = authenticatedUser();
        CreateReviewRequest request = CreateReviewRequest.builder()
                .productRating(5)
                .floristRating(4)
                .build();

        Mockito.when(reviewService.createReview(eq(user.getId()), eq(1L), eq(2L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product for this order"));

        mockMvc.perform(post("/api/orders/1/products/2/reviews")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)));
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
