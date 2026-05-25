package com.petal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petal.dto.SavedDateRequest;
import com.petal.dto.SavedDateResponse;
import com.petal.entity.User;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.SavedDateService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavedDateController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavedDateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavedDateService savedDateService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createDateSavesImportantDateForAuthenticatedBuyer() throws Exception {
        User buyer = authenticatedBuyer();
        SavedDateRequest request = savedDateRequest("Mom's Birthday", LocalDate.of(1990, 2, 14), true);
        Mockito.when(savedDateService.createSavedDate(eq(buyer), eq(request))).thenReturn(savedDateResponse());

        mockMvc.perform(post("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Date saved.")))
                .andExpect(jsonPath("$.data.label", is("Mom's Birthday")))
                .andExpect(jsonPath("$.data.recurring", is(true)))
                .andExpect(jsonPath("$.data.nextOccurrenceDate", is("2026-02-14")))
                .andExpect(jsonPath("$.data.reminderDate", is("2026-02-11")))
                .andExpect(jsonPath("$.data.reminderDue", is(false)))
                .andExpect(jsonPath("$.data.reminderSentForYear", is(false)));
    }

    @Test
    void getDatesReturnsOnlyAuthenticatedBuyersDates() throws Exception {
        User buyer = authenticatedBuyer();
        Mockito.when(savedDateService.getSavedDates(buyer)).thenReturn(List.of(savedDateResponse()));

        mockMvc.perform(get("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].label", is("Mom's Birthday")))
                .andExpect(jsonPath("$.data[0].nextOccurrenceDate", is("2026-02-14")))
                .andExpect(jsonPath("$.data[0].reminderDate", is("2026-02-11")))
                .andExpect(jsonPath("$.data[0].reminderDue", is(false)))
                .andExpect(jsonPath("$.data[0].reminderSentForYear", is(false)));
    }

    @Test
    void createDateRejectsBlankLabel() throws Exception {
        authenticatedBuyer();

        mockMvc.perform(post("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedDateRequest(" ", LocalDate.now().plusDays(5), false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.data.label", is("Important date label is required")));
    }

    @Test
    void createDateRejectsMissingEventDate() throws Exception {
        authenticatedBuyer();

        mockMvc.perform(post("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "label", "Anniversary",
                                "recurring", true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.eventDate", is("Event date is required")));
    }

    @Test
    void createDateRejectsMissingRecurringFlag() throws Exception {
        authenticatedBuyer();

        mockMvc.perform(post("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "label", "Anniversary",
                                "eventDate", LocalDate.now().plusDays(5).toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.recurring", is("Recurring flag is required")));
    }

    @Test
    void floristCannotAccessBuyerSavedDateEndpoints() throws Exception {
        User florist = authenticatedFlorist();
        SavedDateRequest request = savedDateRequest("Anniversary", LocalDate.now().plusDays(5), false);
        Mockito.when(savedDateService.createSavedDate(eq(florist), eq(request)))
                .thenThrow(new IllegalArgumentException("Buyer access is required"));

        mockMvc.perform(post("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Buyer access is required")));
    }

    @Test
    void floristCannotListBuyerSavedDateEndpoints() throws Exception {
        User florist = authenticatedFlorist();
        Mockito.when(savedDateService.getSavedDates(florist))
                .thenThrow(new IllegalArgumentException("Buyer access is required"));

        mockMvc.perform(get("/api/users/dates")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isBadRequest())
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
                .name("Karl Florist")
                .email("florist@example.com")
                .role("ROLE_FLORIST")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }

    private SavedDateRequest savedDateRequest(String label, LocalDate eventDate, Boolean recurring) {
        return SavedDateRequest.builder()
                .label(label)
                .eventDate(eventDate)
                .recurring(recurring)
                .build();
    }

    private SavedDateResponse savedDateResponse() {
        return SavedDateResponse.builder()
                .id(8L)
                .label("Mom's Birthday")
                .eventDate(LocalDate.of(1990, 2, 14))
                .recurring(true)
                .nextOccurrenceDate(LocalDate.of(2026, 2, 14))
                .reminderDate(LocalDate.of(2026, 2, 11))
                .reminderDue(false)
                .reminderSentForYear(false)
                .build();
    }
}
