package com.petal.service;

import com.petal.dto.FloristRequest;
import com.petal.dto.FloristResponse;
import com.petal.entity.Florist;
import com.petal.entity.User;
import com.petal.repository.FloristRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FloristService {

    private static final String DEFAULT_CITY = "Cebu, Philippines";
    private static final String DEFAULT_DELIVERY_COVERAGE = "Cebu City, Mandaue, Lapu-Lapu";
    private static final String DEFAULT_TIME_SLOTS = "AM,PM";
    private static final int DEFAULT_DAILY_CAPACITY = 12;
    private static final int DEFAULT_PREP_LEAD_TIME_HOURS = 24;

    private final FloristRepository floristRepository;

    public Florist createDefaultProfile(User user) {
        requireFloristUser(user);

        return floristRepository.findByUser(user)
                .orElseGet(() -> floristRepository.save(Florist.builder()
                        .user(user)
                        .storeName(defaultStoreName(user))
                        .bio("Locally composed preserved floral pieces for thoughtful Cebu gifting.")
                        .city(DEFAULT_CITY)
                        .maxDailyCapacity(DEFAULT_DAILY_CAPACITY)
                        .deliveryCoverage(DEFAULT_DELIVERY_COVERAGE)
                        .timeSlots(DEFAULT_TIME_SLOTS)
                        .prepLeadTimeHours(DEFAULT_PREP_LEAD_TIME_HOURS)
                        .onboardingComplete(false)
                        .build()));
    }

    public Florist getOrCreateForUser(User user) {
        return createDefaultProfile(user);
    }

    public FloristResponse getCurrentFlorist(User user) {
        return toResponse(getOrCreateForUser(user));
    }

    public FloristResponse updateCurrentFlorist(User user, FloristRequest request) {
        Florist florist = getOrCreateForUser(user);

        if (request.getStoreName() != null) {
            florist.setStoreName(blankToNull(request.getStoreName()));
        }
        if (request.getBio() != null) {
            florist.setBio(blankToNull(request.getBio()));
        }
        if (request.getLogoUrl() != null) {
            florist.setLogoUrl(blankToNull(request.getLogoUrl()));
        }
        if (request.getStreet() != null) {
            florist.setStreet(blankToNull(request.getStreet()));
        }
        if (request.getCity() != null) {
            florist.setCity(blankToNull(request.getCity()));
        }
        if (request.getZipCode() != null) {
            florist.setZipCode(blankToNull(request.getZipCode()));
        }
        if (request.getMaxDailyCapacity() != null) {
            florist.setMaxDailyCapacity(request.getMaxDailyCapacity());
        }
        if (request.getDeliveryCoverage() != null) {
            florist.setDeliveryCoverage(blankToNull(request.getDeliveryCoverage()));
        }
        if (request.getTimeSlots() != null) {
            florist.setTimeSlots(blankToNull(request.getTimeSlots()));
        }
        if (request.getPrepLeadTimeHours() != null) {
            florist.setPrepLeadTimeHours(request.getPrepLeadTimeHours());
        }
        if (request.getOnboardingComplete() != null) {
            florist.setOnboardingComplete(request.getOnboardingComplete());
        }

        return toResponse(floristRepository.save(florist));
    }

    public FloristResponse toResponse(Florist florist) {
        return FloristResponse.builder()
                .id(florist.getId())
                .userId(florist.getUser().getId())
                .storeName(florist.getStoreName())
                .bio(florist.getBio())
                .logoUrl(florist.getLogoUrl())
                .street(florist.getStreet())
                .city(florist.getCity())
                .zipCode(florist.getZipCode())
                .maxDailyCapacity(florist.getMaxDailyCapacity())
                .deliveryCoverage(florist.getDeliveryCoverage())
                .timeSlots(florist.getTimeSlots())
                .prepLeadTimeHours(florist.getPrepLeadTimeHours())
                .onboardingComplete(florist.isOnboardingComplete())
                .build();
    }

    private void requireFloristUser(User user) {
        if (user == null || !"ROLE_FLORIST".equals(user.getRole())) {
            throw new IllegalArgumentException("Florist access is required");
        }
    }

    private String defaultStoreName(User user) {
        return user.getName() + "'s Studio";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
