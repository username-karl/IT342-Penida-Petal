package com.petal.service;

import com.petal.entity.SavedDate;
import com.petal.entity.User;
import com.petal.repository.SavedDateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImportantDateReminderSchedulerTest {

    @Mock
    private SavedDateRepository savedDateRepository;

    @Mock
    private ImportantDateNotificationService notificationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ImportantDateReminderScheduler scheduler;

    @Test
    void triggersRemindersForSavedDatesExactlyThreeDaysAway() {
        setClock(LocalDate.of(2026, 5, 20));
        SavedDate due = savedDate("Anniversary", LocalDate.of(2026, 5, 23), false, null);
        Mockito.when(savedDateRepository.findAll()).thenReturn(List.of(due));

        scheduler.processDueReminders();

        verify(notificationService).sendReminder(due, 2026);
        verify(savedDateRepository).save(due);
        org.assertj.core.api.Assertions.assertThat(due.getNotifiedYear()).isEqualTo(2026);
    }

    @Test
    void doesNotTriggerDatesTwoOrFourDaysAway() {
        setClock(LocalDate.of(2026, 5, 20));
        SavedDate twoDaysAway = savedDate("Soon", LocalDate.of(2026, 5, 22), false, null);
        SavedDate fourDaysAway = savedDate("Later", LocalDate.of(2026, 5, 24), false, null);
        Mockito.when(savedDateRepository.findAll()).thenReturn(List.of(twoDaysAway, fourDaysAway));

        scheduler.processDueReminders();

        verify(notificationService, never()).sendReminder(Mockito.any(), Mockito.anyInt());
        verify(savedDateRepository, never()).save(Mockito.any());
    }

    @Test
    void doesNotDuplicateWhenAlreadyNotifiedForTargetEventYear() {
        setClock(LocalDate.of(2026, 5, 20));
        SavedDate alreadyNotified = savedDate("Anniversary", LocalDate.of(2026, 5, 23), false, 2026);
        Mockito.when(savedDateRepository.findAll()).thenReturn(List.of(alreadyNotified));

        scheduler.processDueReminders();

        verify(notificationService, never()).sendReminder(Mockito.any(), Mockito.anyInt());
        verify(savedDateRepository, never()).save(Mockito.any());
    }

    @Test
    void recurringDatesMatchByMonthAndDayNotOriginalYear() {
        setClock(LocalDate.of(2026, 5, 20));
        SavedDate birthday = savedDate("Mom's Birthday", LocalDate.of(1975, 5, 23), true, null);
        Mockito.when(savedDateRepository.findAll()).thenReturn(List.of(birthday));

        scheduler.processDueReminders();

        verify(notificationService).sendReminder(birthday, 2026);
        org.assertj.core.api.Assertions.assertThat(birthday.getNotifiedYear()).isEqualTo(2026);
    }

    @Test
    void yearBoundaryUsesTargetEventYearForNotifiedYear() {
        setClock(LocalDate.of(2026, 12, 29));
        SavedDate newYearBirthday = savedDate("New Year Birthday", LocalDate.of(1990, 1, 1), true, null);
        Mockito.when(savedDateRepository.findAll()).thenReturn(List.of(newYearBirthday));

        scheduler.processDueReminders();

        verify(notificationService).sendReminder(newYearBirthday, 2027);
        org.assertj.core.api.Assertions.assertThat(newYearBirthday.getNotifiedYear()).isEqualTo(2027);
    }

    private void setClock(LocalDate today) {
        ZoneId zone = ZoneId.of("Asia/Manila");
        Mockito.when(clock.instant()).thenReturn(today.atStartOfDay(zone).toInstant());
        Mockito.when(clock.getZone()).thenReturn(zone);
    }

    private SavedDate savedDate(String label, LocalDate eventDate, boolean recurring, Integer notifiedYear) {
        return SavedDate.builder()
                .id(8L)
                .user(User.builder().id(2L).name("Mikaela Santos").email("mika@example.com").role("ROLE_BUYER").build())
                .label(label)
                .eventDate(eventDate)
                .recurring(recurring)
                .notifiedYear(notifiedYear)
                .build();
    }
}
