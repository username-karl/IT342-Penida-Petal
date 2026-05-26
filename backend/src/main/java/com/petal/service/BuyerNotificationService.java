package com.petal.service;

import com.petal.dto.BuyerNotificationResponse;
import com.petal.entity.BuyerNotification;
import com.petal.entity.SavedDate;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.BuyerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BuyerNotificationService implements ImportantDateNotificationService {

    public static final String IMPORTANT_DATE_REMINDER = "IMPORTANT_DATE_REMINDER";

    private static final DateTimeFormatter EVENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH);

    private final BuyerNotificationRepository buyerNotificationRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void sendReminder(SavedDate savedDate, int notificationYear) {
        if (buyerNotificationRepository.existsByBuyerAndTypeAndSavedDateAndNotificationYear(
                savedDate.getUser(),
                IMPORTANT_DATE_REMINDER,
                savedDate,
                notificationYear
        )) {
            return;
        }

        LocalDate eventDate = occurrenceDate(savedDate, notificationYear);
        BuyerNotification notification = BuyerNotification.builder()
                .buyer(savedDate.getUser())
                .savedDate(savedDate)
                .type(IMPORTANT_DATE_REMINDER)
                .title(savedDate.getLabel() + " is coming up")
                .message(savedDate.getLabel() + " is on " + EVENT_DATE_FORMATTER.format(eventDate)
                        + ". Choose flowers now so the gift feels thoughtful, not rushed.")
                .eventDate(eventDate)
                .notificationYear(notificationYear)
                .createdAt(Instant.now(clock))
                .build();

        buyerNotificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<BuyerNotificationResponse> getNotifications(User buyer, boolean unreadOnly) {
        requireBuyer(buyer);
        List<BuyerNotification> notifications = unreadOnly
                ? buyerNotificationRepository.findByBuyerAndReadAtIsNullOrderByCreatedAtDescIdDesc(buyer)
                : buyerNotificationRepository.findByBuyerOrderByCreatedAtDescIdDesc(buyer);
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional
    public BuyerNotificationResponse markRead(User buyer, Long id) {
        requireBuyer(buyer);
        BuyerNotification notification = buyerNotificationRepository.findByIdAndBuyer(id, buyer)
                .orElseThrow(() -> new ForbiddenException("Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now(clock));
        }
        return toResponse(buyerNotificationRepository.save(notification));
    }

    private LocalDate occurrenceDate(SavedDate savedDate, int notificationYear) {
        if (!savedDate.isRecurring()) {
            return savedDate.getEventDate();
        }

        YearMonth yearMonth = YearMonth.of(notificationYear, savedDate.getEventDate().getMonth());
        int day = Math.min(savedDate.getEventDate().getDayOfMonth(), yearMonth.lengthOfMonth());
        return LocalDate.of(notificationYear, savedDate.getEventDate().getMonth(), day);
    }

    private void requireBuyer(User user) {
        if (user == null || !"ROLE_BUYER".equals(user.getRole())) {
            throw new ForbiddenException("Buyer access is required");
        }
    }

    private BuyerNotificationResponse toResponse(BuyerNotification notification) {
        return BuyerNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .savedDateId(notification.getSavedDate().getId())
                .eventDate(notification.getEventDate())
                .notificationYear(notification.getNotificationYear())
                .read(notification.getReadAt() != null)
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
