package com.petal.service;

import com.petal.dto.SavedDateRequest;
import com.petal.dto.SavedDateResponse;
import com.petal.entity.SavedDate;
import com.petal.entity.User;
import com.petal.repository.SavedDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedDateService {

    private final SavedDateRepository savedDateRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<SavedDateResponse> getSavedDates(User user) {
        requireBuyer(user);
        return savedDateRepository.findByUserOrderByEventDateAscIdAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SavedDateResponse createSavedDate(User user, SavedDateRequest request) {
        requireBuyer(user);
        validateDateRules(request);

        SavedDate savedDate = SavedDate.builder()
                .user(user)
                .label(request.getLabel().trim())
                .eventDate(request.getEventDate())
                .recurring(request.getRecurring())
                .build();

        return toResponse(savedDateRepository.save(savedDate));
    }

    private void validateDateRules(SavedDateRequest request) {
        if (Boolean.FALSE.equals(request.getRecurring())
                && request.getEventDate().isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Non-recurring event date cannot be in the past");
        }
    }

    private void requireBuyer(User user) {
        if (user == null || !"ROLE_BUYER".equals(user.getRole())) {
            throw new IllegalArgumentException("Buyer access is required");
        }
    }

    private SavedDateResponse toResponse(SavedDate savedDate) {
        return SavedDateResponse.builder()
                .id(savedDate.getId())
                .label(savedDate.getLabel())
                .eventDate(savedDate.getEventDate())
                .recurring(savedDate.isRecurring())
                .notifiedYear(savedDate.getNotifiedYear())
                .build();
    }
}
