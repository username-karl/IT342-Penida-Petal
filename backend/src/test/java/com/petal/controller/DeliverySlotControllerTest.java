package com.petal.controller;

import com.petal.dto.DeliverySlotAvailabilityResponse;
import com.petal.dto.DeliverySlotResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.DeliverySlotAvailabilityService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliverySlotController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliverySlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliverySlotAvailabilityService deliverySlotAvailabilityService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getAvailabilityReturnsDailyAmAndPmSlotData() throws Exception {
        User buyer = authenticatedBuyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(2);

        Mockito.when(deliverySlotAvailabilityService.getAvailability(eq(buyer), eq(9L), eq(deliveryDate)))
                .thenReturn(DeliverySlotAvailabilityResponse.builder()
                        .date(deliveryDate)
                        .am(DeliverySlotResponse.builder().available(true).remaining(3).build())
                        .pm(DeliverySlotResponse.builder().available(false).remaining(0).build())
                        .build());

        mockMvc.perform(get("/api/slots/availability")
                        .param("florist_id", "9")
                        .param("date", deliveryDate.toString())
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.date", is(deliveryDate.toString())))
                .andExpect(jsonPath("$.data.am.available", is(true)))
                .andExpect(jsonPath("$.data.am.remaining", is(3)))
                .andExpect(jsonPath("$.data.pm.available", is(false)))
                .andExpect(jsonPath("$.data.pm.remaining", is(0)));
    }

    @Test
    void getAvailabilityReturnsForbiddenForNonBuyerUsers() throws Exception {
        User florist = authenticatedFlorist();
        LocalDate deliveryDate = LocalDate.now().plusDays(2);

        Mockito.when(deliverySlotAvailabilityService.getAvailability(eq(florist), eq(9L), eq(deliveryDate)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Buyer access is required"));

        mockMvc.perform(get("/api/slots/availability")
                        .param("florist_id", "9")
                        .param("date", deliveryDate.toString())
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Buyer access is required")));
    }

    private User authenticatedBuyer() {
        User user = User.builder()
                .id(2L)
                .name("Mikaela Santos")
                .email("mika@example.com")
                .role("ROLE_BUYER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }

    private User authenticatedFlorist() {
        User user = User.builder()
                .id(4L)
                .name("Karl")
                .email("karl@petal.test")
                .role("ROLE_FLORIST")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }
}
