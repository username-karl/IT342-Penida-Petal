package com.petal.service;

import com.petal.dto.DeliverySlotAvailabilityResponse;
import com.petal.dto.DeliverySlotResponse;
import com.petal.entity.Florist;
import com.petal.entity.User;
import com.petal.repository.FloristRepository;
import com.petal.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliverySlotAvailabilityService {

    private static final String FULLY_BOOKED_MESSAGE =
            "This florist is fully booked for this date. Please choose another date.";
    private static final int DEFAULT_DAILY_CAPACITY = 12;

    private final FloristRepository floristRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DeliverySlotAvailabilityResponse getAvailability(User user, Long floristId, LocalDate date) {
        requireBuyer(user);
        Florist florist = floristRepository.findById(floristId)
                .orElseThrow(() -> new IllegalArgumentException("Florist not found"));
        return buildAvailability(florist, date);
    }

    @Transactional(readOnly = true)
    public void validateOrderSlot(User user, Long floristId, LocalDate date, String timeSlot) {
        requireBuyer(user);
        if (date == null) {
            throw new IllegalArgumentException("Delivery date is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Delivery date cannot be in the past");
        }

        Florist florist = floristRepository.findById(floristId)
                .orElseThrow(() -> new IllegalArgumentException("Florist not found"));
        if (!supportedSlots(florist.getTimeSlots()).contains(normalizeSlot(timeSlot))) {
            throw new IllegalArgumentException("Selected delivery time slot is unavailable");
        }
        DeliverySlotAvailabilityResponse availability = buildAvailability(florist, date);
        DeliverySlotResponse slot = "PM".equals(normalizeSlot(timeSlot))
                ? availability.getPm()
                : availability.getAm();

        if (!slot.isAvailable()) {
            throw new IllegalArgumentException(slot.getRemaining() <= 0
                    ? FULLY_BOOKED_MESSAGE
                    : "Selected delivery time slot is unavailable");
        }
    }

    private DeliverySlotAvailabilityResponse buildAvailability(Florist florist, LocalDate date) {
        Set<String> supportedSlots = supportedSlots(florist.getTimeSlots());
        int capacity = florist.getMaxDailyCapacity() == null ? DEFAULT_DAILY_CAPACITY : florist.getMaxDailyCapacity();
        long activeOrders = orderRepository.countDistinctActiveOrdersByFloristIdAndDeliveryDate(florist.getId(), date);
        int remaining = Math.max(0, capacity - Math.toIntExact(activeOrders));
        boolean dateAvailable = date != null && !date.isBefore(LocalDate.now()) && remaining > 0;

        return DeliverySlotAvailabilityResponse.builder()
                .date(date)
                .am(slotResponse(supportedSlots.contains("AM") && dateAvailable, remaining))
                .pm(slotResponse(supportedSlots.contains("PM") && dateAvailable, remaining))
                .build();
    }

    private DeliverySlotResponse slotResponse(boolean available, int remaining) {
        return DeliverySlotResponse.builder()
                .available(available)
                .remaining(available ? remaining : 0)
                .build();
    }

    private Set<String> supportedSlots(String timeSlots) {
        if (timeSlots == null || timeSlots.isBlank()) {
            return Set.of("AM", "PM");
        }
        return Arrays.stream(timeSlots.split(","))
                .map(this::normalizeSlot)
                .filter(slot -> !slot.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizeSlot(String slot) {
        return slot == null ? "" : slot.trim().toUpperCase(Locale.ROOT);
    }

    private void requireBuyer(User user) {
        if (user == null || !"ROLE_BUYER".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Buyer access is required");
        }
    }
}
