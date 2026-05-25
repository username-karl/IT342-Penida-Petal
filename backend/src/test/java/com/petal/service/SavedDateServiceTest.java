package com.petal.service;

import com.petal.dto.SavedDateRequest;
import com.petal.dto.SavedDateResponse;
import com.petal.entity.SavedDate;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.SavedDateRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SavedDateServiceTest {

    @Mock
    private SavedDateRepository savedDateRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private SavedDateService savedDateService;

    @BeforeEach
    void setDefaultClock() {
        setClock(LocalDate.of(2026, 5, 20));
    }

    @Test
    void createSavedDateTrimsLabelAndStoresAuthenticatedBuyerOwner() {
        User buyer = buyer();
        SavedDateRequest request = request("  Mom's Birthday  ", LocalDate.of(1990, 2, 14), true);
        Mockito.when(savedDateRepository.save(Mockito.any(SavedDate.class))).thenAnswer(invocation -> {
            SavedDate savedDate = invocation.getArgument(0);
            savedDate.setId(12L);
            return savedDate;
        });

        SavedDateResponse response = savedDateService.createSavedDate(buyer, request);

        ArgumentCaptor<SavedDate> captor = ArgumentCaptor.forClass(SavedDate.class);
        Mockito.verify(savedDateRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(buyer);
        assertThat(captor.getValue().getLabel()).isEqualTo("Mom's Birthday");
        assertThat(captor.getValue().getEventDate()).isEqualTo(LocalDate.of(1990, 2, 14));
        assertThat(captor.getValue().isRecurring()).isTrue();
        assertThat(response.getId()).isEqualTo(12L);
    }

    @Test
    void responseIncludesReminderMetadataForUpcomingOneTimeDate() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Anniversary", LocalDate.of(2026, 5, 24), false, null)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.getNextOccurrenceDate()).isEqualTo(LocalDate.of(2026, 5, 24));
        assertThat(response.getReminderDate()).isEqualTo(LocalDate.of(2026, 5, 21));
        assertThat(response.isReminderDue()).isFalse();
        assertThat(response.isReminderSentForYear()).isFalse();
    }

    @Test
    void responseMarksReminderDueWhenTodayIsThreeDaysBeforeEvent() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Anniversary", LocalDate.of(2026, 5, 23), false, null)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.getReminderDate()).isEqualTo(LocalDate.of(2026, 5, 20));
        assertThat(response.isReminderDue()).isTrue();
        assertThat(response.isReminderSentForYear()).isFalse();
    }

    @Test
    void responseMarksReminderSentForMatchingNotifiedYear() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Anniversary", LocalDate.of(2026, 5, 23), false, 2026)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.isReminderDue()).isFalse();
        assertThat(response.isReminderSentForYear()).isTrue();
    }

    @Test
    void recurringDateUsesNextOccurrenceInTheCurrentYearWhenUpcoming() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Birthday", LocalDate.of(1990, 5, 25), true, null)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.getNextOccurrenceDate()).isEqualTo(LocalDate.of(2026, 5, 25));
        assertThat(response.getReminderDate()).isEqualTo(LocalDate.of(2026, 5, 22));
    }

    @Test
    void recurringDateUsesNextYearWhenCurrentYearOccurrencePassed() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Birthday", LocalDate.of(1990, 5, 19), true, null)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.getNextOccurrenceDate()).isEqualTo(LocalDate.of(2027, 5, 19));
        assertThat(response.getReminderDate()).isEqualTo(LocalDate.of(2027, 5, 16));
    }

    @Test
    void recurringDateHandlesReminderAcrossYearBoundary() {
        setClock(LocalDate.of(2026, 12, 29));
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "New Year Birthday", LocalDate.of(1990, 1, 1), true, null)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.getNextOccurrenceDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(response.getReminderDate()).isEqualTo(LocalDate.of(2026, 12, 29));
        assertThat(response.isReminderDue()).isTrue();
    }

    @Test
    void recurringLeapDayUsesLastValidDayInNonLeapYears() {
        setClock(LocalDate.of(2027, 2, 25));
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Leap Day Birthday", LocalDate.of(2024, 2, 29), true, null)));

        SavedDateResponse response = savedDateService.getSavedDates(buyer).get(0);

        assertThat(response.getNextOccurrenceDate()).isEqualTo(LocalDate.of(2027, 2, 28));
        assertThat(response.getReminderDate()).isEqualTo(LocalDate.of(2027, 2, 25));
        assertThat(response.isReminderDue()).isTrue();
    }

    @Test
    void getSavedDatesReturnsOnlyAuthenticatedBuyersDates() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.findByUserOrderByEventDateAscIdAsc(buyer))
                .thenReturn(List.of(savedDate(buyer, "Anniversary", LocalDate.of(2026, 6, 4), false, null)));

        List<SavedDateResponse> response = savedDateService.getSavedDates(buyer);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getLabel()).isEqualTo("Anniversary");
        Mockito.verify(savedDateRepository).findByUserOrderByEventDateAscIdAsc(buyer);
    }

    @Test
    void floristCannotCreateSavedDate() {
        assertThatThrownBy(() -> savedDateService.createSavedDate(florist(), request("Anniversary", LocalDate.now().plusDays(5), false)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Buyer access is required");
    }

    @Test
    void floristCannotListSavedDates() {
        assertThatThrownBy(() -> savedDateService.getSavedDates(florist()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Buyer access is required");
    }

    @Test
    void nonRecurringPastDatesAreRejected() {
        setClock(LocalDate.of(2026, 5, 20));

        assertThatThrownBy(() -> savedDateService.createSavedDate(
                buyer(),
                request("Past delivery reminder", LocalDate.of(2026, 5, 19), false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Non-recurring event date cannot be in the past");
    }

    @Test
    void recurringHistoricalDatesAreAllowed() {
        User buyer = buyer();
        Mockito.when(savedDateRepository.save(Mockito.any(SavedDate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SavedDateResponse response = savedDateService.createSavedDate(
                buyer,
                request("Mom's Birthday", LocalDate.of(1975, 5, 19), true));

        assertThat(response.getEventDate()).isEqualTo(LocalDate.of(1975, 5, 19));
        assertThat(response.isRecurring()).isTrue();
    }

    private void setClock(LocalDate today) {
        ZoneId zone = ZoneId.of("Asia/Manila");
        Mockito.lenient().when(clock.instant()).thenReturn(today.atStartOfDay(zone).toInstant());
        Mockito.lenient().when(clock.getZone()).thenReturn(zone);
    }

    private SavedDateRequest request(String label, LocalDate eventDate, Boolean recurring) {
        return SavedDateRequest.builder()
                .label(label)
                .eventDate(eventDate)
                .recurring(recurring)
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

    private SavedDate savedDate(User user, String label, LocalDate eventDate, boolean recurring, Integer notifiedYear) {
        return SavedDate.builder()
                .id(8L)
                .user(user)
                .label(label)
                .eventDate(eventDate)
                .recurring(recurring)
                .notifiedYear(notifiedYear)
                .build();
    }
}
