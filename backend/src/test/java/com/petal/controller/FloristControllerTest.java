package com.petal.controller;

import com.petal.dto.FloristResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.FloristService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({FloristController.class, FloristProfileImageController.class})
@AutoConfigureMockMvc(addFilters = false)
class FloristControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FloristService floristService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getCurrentFloristReturnsSellerProfile() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(floristService.getCurrentFlorist(seller)).thenReturn(FloristResponse.builder()
                .id(12L)
                .userId(4L)
                .storeName("Cebu Florist Studio")
                .bio("Preserved floral pieces from Cebu.")
                .city("Cebu, Philippines")
                .maxDailyCapacity(14)
                .deliveryCoverage("Cebu City")
                .timeSlots("AM,PM")
                .prepLeadTimeHours(24)
                .onboardingComplete(false)
                .build());

        mockMvc.perform(get("/api/seller/florist")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(12)))
                .andExpect(jsonPath("$.data.storeName", is("Cebu Florist Studio")))
                .andExpect(jsonPath("$.data.timeSlots", is("AM,PM")));
    }

    @Test
    void updateCurrentFloristReturnsSavedSellerProfile() throws Exception {
        User seller = authenticatedSeller();
        Mockito.when(floristService.updateCurrentFlorist(eq(seller), any())).thenReturn(FloristResponse.builder()
                .id(12L)
                .userId(4L)
                .storeName("Cebu Florist Studio")
                .bio("Morning delivery arrangements.")
                .city("Cebu City")
                .maxDailyCapacity(18)
                .deliveryCoverage("Cebu City, Mandaue")
                .timeSlots("AM,PM")
                .prepLeadTimeHours(18)
                .onboardingComplete(true)
                .build());

        mockMvc.perform(put("/api/seller/florist")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeName": "Cebu Florist Studio",
                                  "bio": "Morning delivery arrangements.",
                                  "city": "Cebu City",
                                  "maxDailyCapacity": 18,
                                  "deliveryCoverage": "Cebu City, Mandaue",
                                  "timeSlots": "AM,PM",
                                  "prepLeadTimeHours": 18,
                                  "onboardingComplete": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.city", is("Cebu City")))
                .andExpect(jsonPath("$.data.maxDailyCapacity", is(18)))
                .andExpect(jsonPath("$.data.onboardingComplete", is(true)));
    }

    @Test
    void uploadProfileImageUsesMultipartFileField() throws Exception {
        User seller = authenticatedSeller();
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "image-bytes".getBytes());

        Mockito.when(floristService.uploadProfileImage(eq(seller), any())).thenReturn(FloristResponse.builder()
                .id(12L)
                .userId(4L)
                .storeName("Cebu Florist Studio")
                .logoUrl("/uploads/florist-logos/logo.png")
                .build());

        mockMvc.perform(multipart("/api/florists/profile/image")
                        .file(file)
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.logoUrl", is("/uploads/florist-logos/logo.png")));
    }

    @Test
    void compatibleSellerRouteUploadsProfileImage() throws Exception {
        User seller = authenticatedSeller();
        MockMultipartFile file = new MockMultipartFile("file", "logo.jpg", "image/jpeg", "image-bytes".getBytes());

        Mockito.when(floristService.uploadProfileImage(eq(seller), any())).thenReturn(FloristResponse.builder()
                .id(12L)
                .userId(4L)
                .logoUrl("/uploads/florist-logos/logo.jpg")
                .build());

        mockMvc.perform(multipart("/api/seller/florist/image")
                        .file(file)
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logoUrl", is("/uploads/florist-logos/logo.jpg")));
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
