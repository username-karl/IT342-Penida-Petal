package com.petal.service;

import com.petal.dto.BuyerNotificationResponse;
import com.petal.entity.BuyerNotification;
import com.petal.entity.SavedDate;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.BuyerNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BuyerNotificationServiceTest {

    @Mock
    private BuyerNotificationRepository buyerNotificationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private BuyerNotificationService buyerNotificationService;

    @Test
    void createsReminderNotificationForDueSavedDate() {
        setClock(Instant.parse("2026-05-20T00:00:00Z"));
        SavedDate savedDate = savedDate();
        Mockito.when(buyerNotificationRepository.existsByBuyerAndTypeAndSavedDateAndNotificationYear(
                savedDate.getUser(),
                "IMPORTANT_DATE_REMINDER",
                savedDate,
                2026
        )).thenReturn(false);
        Mockito.when(buyerNotificationRepository.save(Mockito.any(BuyerNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        buyerNotificationService.sendReminder(savedDate, 2026);

        ArgumentCaptor<BuyerNotification> captor = ArgumentCaptor.forClass(BuyerNotification.class);
        verify(buyerNotificationRepository).save(captor.capture());
        BuyerNotification notification = captor.getValue();
        assertThat(notification.getBuyer()).isEqualTo(savedDate.getUser());
        assertThat(notification.getSavedDate()).isEqualTo(savedDate);
        assertThat(notification.getType()).isEqualTo("IMPORTANT_DATE_REMINDER");
        assertThat(notification.getTitle()).isEqualTo("Mom's Birthday is coming up");
        assertThat(notification.getMessage()).contains("May 23");
        assertThat(notification.getEventDate()).isEqualTo(LocalDate.of(2026, 5, 23));
        assertThat(notification.getNotificationYear()).isEqualTo(2026);
        assertThat(notification.getCreatedAt()).isEqualTo(Instant.parse("2026-05-20T00:00:00Z"));
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    void skipsDuplicateReminderNotificationForSameSavedDateAndYear() {
        SavedDate savedDate = savedDate();
        Mockito.when(buyerNotificationRepository.existsByBuyerAndTypeAndSavedDateAndNotificationYear(
                savedDate.getUser(),
                "IMPORTANT_DATE_REMINDER",
                savedDate,
                2026
        )).thenReturn(true);

        buyerNotificationService.sendReminder(savedDate, 2026);

        verify(buyerNotificationRepository, never()).save(Mockito.any());
    }

    @Test
    void listNotificationsReturnsCurrentBuyerNewestFirst() {
        User buyer = buyer();
        BuyerNotification notification = notification(buyer, null);
        Mockito.when(buyerNotificationRepository.findByBuyerOrderByCreatedAtDescIdDesc(buyer))
                .thenReturn(List.of(notification));

        List<BuyerNotificationResponse> response = buyerNotificationService.getNotifications(buyer, false);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(22L);
        assertThat(response.get(0).isRead()).isFalse();
    }

    @Test
    void listNotificationsCanFilterUnreadOnly() {
        User buyer = buyer();
        BuyerNotification notification = notification(buyer, null);
        Mockito.when(buyerNotificationRepository.findByBuyerAndReadAtIsNullOrderByCreatedAtDescIdDesc(buyer))
                .thenReturn(List.of(notification));

        List<BuyerNotificationResponse> response = buyerNotificationService.getNotifications(buyer, true);

        assertThat(response).hasSize(1);
        verify(buyerNotificationRepository).findByBuyerAndReadAtIsNullOrderByCreatedAtDescIdDesc(buyer);
    }

    @Test
    void markReadSetsReadAtForOwnedNotification() {
        setClock(Instant.parse("2026-05-20T01:00:00Z"));
        User buyer = buyer();
        BuyerNotification notification = notification(buyer, null);
        Mockito.when(buyerNotificationRepository.findByIdAndBuyer(22L, buyer))
                .thenReturn(Optional.of(notification));
        Mockito.when(buyerNotificationRepository.save(notification)).thenReturn(notification);

        BuyerNotificationResponse response = buyerNotificationService.markRead(buyer, 22L);

        assertThat(notification.getReadAt()).isEqualTo(Instant.parse("2026-05-20T01:00:00Z"));
        assertThat(response.isRead()).isTrue();
    }

    @Test
    void markReadIsIdempotentForAlreadyReadNotification() {
        setClock(Instant.parse("2026-05-20T01:00:00Z"));
        Instant originalReadAt = Instant.parse("2026-05-19T01:00:00Z");
        User buyer = buyer();
        BuyerNotification notification = notification(buyer, originalReadAt);
        Mockito.when(buyerNotificationRepository.findByIdAndBuyer(22L, buyer))
                .thenReturn(Optional.of(notification));
        Mockito.when(buyerNotificationRepository.save(notification)).thenReturn(notification);

        buyerNotificationService.markRead(buyer, 22L);

        assertThat(notification.getReadAt()).isEqualTo(originalReadAt);
    }

    @Test
    void markReadRejectsNotificationOwnedByAnotherBuyer() {
        User buyer = buyer();
        Mockito.when(buyerNotificationRepository.findByIdAndBuyer(22L, buyer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buyerNotificationService.markRead(buyer, 22L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Notification not found");
    }

    @Test
    void floristCannotListNotifications() {
        assertThatThrownBy(() -> buyerNotificationService.getNotifications(florist(), false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Buyer access is required");
    }

    private void setClock(Instant instant) {
        Mockito.lenient().when(clock.instant()).thenReturn(instant);
        Mockito.lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Manila"));
    }

    private BuyerNotification notification(User buyer, Instant readAt) {
        return BuyerNotification.builder()
                .id(22L)
                .buyer(buyer)
                .savedDate(savedDate())
                .type("IMPORTANT_DATE_REMINDER")
                .title("Mom's Birthday is coming up")
                .message("Mom's Birthday is on May 23. Choose flowers now so the gift feels thoughtful, not rushed.")
                .eventDate(LocalDate.of(2026, 5, 23))
                .notificationYear(2026)
                .createdAt(Instant.parse("2026-05-20T00:00:00Z"))
                .readAt(readAt)
                .build();
    }

    private SavedDate savedDate() {
        return SavedDate.builder()
                .id(8L)
                .user(buyer())
                .label("Mom's Birthday")
                .eventDate(LocalDate.of(1990, 5, 23))
                .recurring(true)
                .build();
    }

    private User buyer() {
        return User.builder()
                .id(2L)
                .name("Mikaela Santos")
                .email("mika@example.com")
                .role("ROLE_BUYER")
                .build();
    }

    private User florist() {
        return User.builder()
                .id(4L)
                .name("Karl Florist")
                .email("florist@example.com")
                .role("ROLE_FLORIST")
                .build();
    }
}
