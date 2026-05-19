package com.petal.service;

import com.petal.dto.DeliveryAddressRequest;
import com.petal.dto.DeliveryAddressResponse;
import com.petal.entity.DeliveryAddress;
import com.petal.entity.User;
import com.petal.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;

    public List<DeliveryAddressResponse> getAddresses(User user) {
        return deliveryAddressRepository.findByUserOrderByDefaultAddressDescIdAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DeliveryAddressResponse createAddress(User user, DeliveryAddressRequest request) {
        if (request.isDefaultAddress()) {
            deliveryAddressRepository.clearDefaultAddressForUser(user);
        }

        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .label(request.getLabel())
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine(request.getAddressLine())
                .defaultAddress(request.isDefaultAddress())
                .build();

        return toResponse(deliveryAddressRepository.save(address));
    }

    @Transactional
    public DeliveryAddressResponse updateAddress(User user, Long id, DeliveryAddressRequest request) {
        DeliveryAddress address = deliveryAddressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (request.isDefaultAddress()) {
            deliveryAddressRepository.clearDefaultAddressForUser(user);
        }

        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine(request.getAddressLine());
        address.setDefaultAddress(request.isDefaultAddress());

        return toResponse(deliveryAddressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(User user, Long id) {
        DeliveryAddress address = deliveryAddressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        deliveryAddressRepository.delete(address);
    }

    private DeliveryAddressResponse toResponse(DeliveryAddress address) {
        return DeliveryAddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine(address.getAddressLine())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}
