package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.DeliveryAddressRequest;
import com.petal.dto.DeliveryAddressResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.DeliveryAddressService;
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

@WebMvcTest({DeliveryAddressController.class, UserAddressController.class})
@AutoConfigureMockMvc(addFilters = false)
class DeliveryAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryAddressService deliveryAddressService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getAddressesReturnsCurrentUsersSavedAddresses() throws Exception {
        User user = authenticatedUser();
        Mockito.when(deliveryAddressService.getAddresses(user)).thenReturn(List.of(addressResponse()));

        mockMvc.perform(get("/api/addresses")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].recipientName", is("Mika Santos")))
                .andExpect(jsonPath("$.data[0].defaultAddress", is(true)));
    }

    @Test
    void createAddressSavesAddressForCurrentUser() throws Exception {
        User user = authenticatedUser();
        DeliveryAddressRequest request = addressRequest();
        Mockito.when(deliveryAddressService.createAddress(eq(user), eq(request))).thenReturn(addressResponse());

        mockMvc.perform(post("/api/addresses")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Address saved successfully")))
                .andExpect(jsonPath("$.data.label", is("Home")));
    }

    @Test
    void sddUserAddressAliasCreatesAddressForCurrentUser() throws Exception {
        User user = authenticatedUser();
        DeliveryAddressRequest request = addressRequest();
        Mockito.when(deliveryAddressService.createAddress(eq(user), eq(request))).thenReturn(addressResponse());

        mockMvc.perform(post("/api/users/addresses")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Address saved successfully")))
                .andExpect(jsonPath("$.data.label", is("Home")));
    }

    @Test
    void sddUserAddressAliasGetsCurrentUsersAddresses() throws Exception {
        User user = authenticatedUser();
        Mockito.when(deliveryAddressService.getAddresses(user)).thenReturn(List.of(addressResponse()));

        mockMvc.perform(get("/api/users/addresses")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].recipientName", is("Mika Santos")));
    }

    @Test
    void updateAddressUpdatesOnlyCurrentUsersAddress() throws Exception {
        User user = authenticatedUser();
        DeliveryAddressRequest request = addressRequest();
        Mockito.when(deliveryAddressService.updateAddress(eq(user), eq(5L), eq(request))).thenReturn(addressResponse());

        mockMvc.perform(put("/api/addresses/5")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.addressLine", is("Cebu Business Park, Cebu City")));
    }

    @Test
    void deleteAddressRemovesOnlyCurrentUsersAddress() throws Exception {
        User user = authenticatedUser();

        mockMvc.perform(delete("/api/addresses/5")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Address removed")));

        Mockito.verify(deliveryAddressService).deleteAddress(user, 5L);
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

    private DeliveryAddressRequest addressRequest() {
        return DeliveryAddressRequest.builder()
                .label("Home")
                .recipientName("Mika Santos")
                .phoneNumber("09171234567")
                .addressLine("Cebu Business Park, Cebu City")
                .defaultAddress(true)
                .build();
    }

    private DeliveryAddressResponse addressResponse() {
        return DeliveryAddressResponse.builder()
                .id(5L)
                .label("Home")
                .recipientName("Mika Santos")
                .phoneNumber("09171234567")
                .addressLine("Cebu Business Park, Cebu City")
                .defaultAddress(true)
                .build();
    }
}
