package com.petal.controller;

import com.petal.dto.BuyerNotificationResponse;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.UserRepository;
import com.petal.security.JwtUtil;
import com.petal.service.BuyerNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuyerNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class BuyerNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuyerNotificationService buyerNotificationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getNotificationsReturnsCurrentBuyersNotifications() throws Exception {
        User buyer = authenticatedBuyer();
        Mockito.when(buyerNotificationService.getNotifications(buyer, false))
                .thenReturn(List.of(notification(false)));

        mockMvc.perform(get("/api/users/notifications")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Notifications fetched successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("Mom's Birthday is coming up")))
                .andExpect(jsonPath("$.data[0].read", is(false)));
    }

    @Test
    void getNotificationsSupportsUnreadOnlyQuery() throws Exception {
        User buyer = authenticatedBuyer();
        Mockito.when(buyerNotificationService.getNotifications(buyer, true))
                .thenReturn(List.of(notification(false)));

        mockMvc.perform(get("/api/users/notifications?unreadOnly=true")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        Mockito.verify(buyerNotificationService).getNotifications(buyer, true);
    }

    @Test
    void markReadUpdatesOnlyCurrentBuyersNotification() throws Exception {
        User buyer = authenticatedBuyer();
        Mockito.when(buyerNotificationService.markRead(eq(buyer), eq(22L)))
                .thenReturn(notification(true));

        mockMvc.perform(patch("/api/users/notifications/22/read")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Notification marked as read")))
                .andExpect(jsonPath("$.data.read", is(true)))
                .andExpect(jsonPath("$.data.readAt", is("2026-05-20T01:00:00Z")));
    }

    @Test
    void anotherBuyerCannotMarkNotificationRead() throws Exception {
        User buyer = authenticatedBuyer();
        Mockito.when(buyerNotificationService.markRead(eq(buyer), eq(99L)))
                .thenThrow(new ForbiddenException("Notification not found"));

        mockMvc.perform(patch("/api/users/notifications/99/read")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Notification not found")));
    }

    @Test
    void floristCannotListBuyerNotifications() throws Exception {
        User florist = authenticatedFlorist();
        Mockito.when(buyerNotificationService.getNotifications(florist, false))
                .thenThrow(new ForbiddenException("Buyer access is required"));

        mockMvc.perform(get("/api/users/notifications")
                        .principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isForbidden())
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

    private BuyerNotificationResponse notification(boolean read) {
        return BuyerNotificationResponse.builder()
                .id(22L)
                .type("IMPORTANT_DATE_REMINDER")
                .title("Mom's Birthday is coming up")
                .message("Mom's Birthday is on May 23. Choose flowers now so the gift feels thoughtful, not rushed.")
                .savedDateId(8L)
                .eventDate(LocalDate.of(2026, 5, 23))
                .notificationYear(2026)
                .read(read)
                .createdAt(Instant.parse("2026-05-20T00:00:00Z"))
                .readAt(read ? Instant.parse("2026-05-20T01:00:00Z") : null)
                .build();
    }
}
