package com.petal.service;

import com.petal.dto.FloristProfileRequest;
import com.petal.dto.FloristProfileResponse;
import com.petal.entity.Florist;
import com.petal.entity.User;
import com.petal.repository.FloristRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FloristService {

    private final FloristRepository floristRepository;

    /**
     * Gets the florist profile for the given user. Auto-creates one if it doesn't exist.
     */
    public Florist getOrCreateFlorist(User user) {
        return floristRepository.findByUser(user)
                .orElseGet(() -> {
                    Florist florist = Florist.builder()
                            .user(user)
                            .storeName(user.getName() + "'s Studio")
                            .bio("")
                            .build();
                    return floristRepository.save(florist);
                });
    }

    public FloristProfileResponse getProfile(User user) {
        Florist florist = getOrCreateFlorist(user);
        return mapToResponse(florist);
    }

    public FloristProfileResponse updateProfile(User user, FloristProfileRequest request) {
        Florist florist = getOrCreateFlorist(user);

        florist.setStoreName(request.getStoreName());
        florist.setBio(request.getBio());
        florist.setStreet(request.getStreet());
        florist.setCity(request.getCity());
        florist.setZipCode(request.getZipCode());

        Florist saved = floristRepository.save(florist);
        return mapToResponse(saved);
    }

    private FloristProfileResponse mapToResponse(Florist florist) {
        return FloristProfileResponse.builder()
                .id(florist.getId())
                .userId(florist.getUser().getId())
                .storeName(florist.getStoreName())
                .bio(florist.getBio())
                .logoUrl(florist.getLogoUrl())
                .street(florist.getStreet())
                .city(florist.getCity())
                .zipCode(florist.getZipCode())
                .maxDailyCapacity(florist.getMaxDailyCapacity())
                .ownerName(florist.getUser().getName())
                .ownerEmail(florist.getUser().getEmail())
                .build();
    }
}
