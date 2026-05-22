package com.petal.service;

import com.petal.dto.DeliveryAddressRequest;
import com.petal.dto.DeliveryAddressResponse;
import com.petal.entity.DeliveryAddress;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.DeliveryAddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceTest {

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @InjectMocks
    private DeliveryAddressService deliveryAddressService;

    @Test
    void getAddressesReturnsCurrentUsersAddressesInDefaultOrder() {
        User user = buyer();
        Mockito.when(deliveryAddressRepository.findByUserOrderByDefaultAddressDescIdAsc(user))
                .thenReturn(List.of(address(user, true)));

        List<DeliveryAddressResponse> addresses = deliveryAddressService.getAddresses(user);

        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).getRecipientName()).isEqualTo("Mika Santos");
        assertThat(addresses.get(0).isDefaultAddress()).isTrue();
    }

    @Test
    void createDefaultAddressClearsExistingDefaultForUser() {
        User user = buyer();
        DeliveryAddressRequest request = request(true);
        DeliveryAddress saved = address(user, true);

        Mockito.when(deliveryAddressRepository.save(Mockito.any(DeliveryAddress.class))).thenReturn(saved);

        DeliveryAddressResponse response = deliveryAddressService.createAddress(user, request);

        assertThat(response.isDefaultAddress()).isTrue();
        verify(deliveryAddressRepository).clearDefaultAddressForUser(user);
    }

    @Test
    void updateAddressRejectsAddressOwnedByAnotherUser() {
        User user = buyer();
        Mockito.when(deliveryAddressRepository.findByIdAndUser(12L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryAddressService.updateAddress(user, 12L, request(false)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Address not found");
    }

    @Test
    void deleteAddressRejectsAddressOwnedByAnotherUser() {
        User user = buyer();
        Mockito.when(deliveryAddressRepository.findByIdAndUser(12L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryAddressService.deleteAddress(user, 12L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Address not found");
    }

    private User buyer() {
        return User.builder()
                .id(2L)
                .name("Karl")
                .email("karl@example.com")
                .role("ROLE_BUYER")
                .build();
    }

    private DeliveryAddressRequest request(boolean defaultAddress) {
        return DeliveryAddressRequest.builder()
                .label("Home")
                .recipientName("Mika Santos")
                .phoneNumber("09171234567")
                .addressLine("Cebu Business Park, Cebu City")
                .defaultAddress(defaultAddress)
                .build();
    }

    private DeliveryAddress address(User user, boolean defaultAddress) {
        return DeliveryAddress.builder()
                .id(12L)
                .user(user)
                .label("Home")
                .recipientName("Mika Santos")
                .phoneNumber("09171234567")
                .addressLine("Cebu Business Park, Cebu City")
                .defaultAddress(defaultAddress)
                .build();
    }
}
