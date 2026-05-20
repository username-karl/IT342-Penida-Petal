package com.petal.service;

import com.petal.entity.SavedDate;
import com.petal.repository.SavedDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;

@Component
@RequiredArgsConstructor
public class ImportantDateReminderScheduler {

    private final SavedDateRepository savedDateRepository;
    private final ImportantDateNotificationService notificationService;
    private final Clock clock;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processDueReminders() {
        LocalDate target = LocalDate.now(clock).plusDays(3);
        int targetYear = target.getYear();

        savedDateRepository.findAll().stream()
                .filter(savedDate -> isDue(savedDate, target))
                .filter(savedDate -> savedDate.getNotifiedYear() == null || savedDate.getNotifiedYear() != targetYear)
                .forEach(savedDate -> {
                    notificationService.sendReminder(savedDate);
                    savedDate.setNotifiedYear(targetYear);
                    savedDateRepository.save(savedDate);
                });
    }

    private boolean isDue(SavedDate savedDate, LocalDate target) {
        if (savedDate.isRecurring()) {
            return MonthDay.from(savedDate.getEventDate()).equals(MonthDay.from(target));
        }
        return savedDate.getEventDate().equals(target);
    }
}
