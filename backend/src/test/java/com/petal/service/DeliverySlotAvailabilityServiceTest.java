package com.petal.service;

import com.petal.dto.DeliverySlotAvailabilityResponse;
import com.petal.entity.Florist;
import com.petal.entity.User;
import com.petal.repository.FloristRepository;
import com.petal.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverySlotAvailabilityServiceTest {

    @Mock
    private FloristRepository floristRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DeliverySlotAvailabilityService deliverySlotAvailabilityService;

    @Test
    void getAvailabilityReturnsAmAndPmRemainingCapacityForBuyer() {
        User buyer = buyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(2);
        Florist florist = Florist.builder()
                .id(9L)
                .maxDailyCapacity(4)
                .timeSlots("AM,PM")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));
        when(orderRepository.countDistinctActiveOrdersByFloristIdAndDeliveryDate(9L, deliveryDate))
                .thenReturn(1L);

        DeliverySlotAvailabilityResponse response =
                deliverySlotAvailabilityService.getAvailability(buyer, 9L, deliveryDate);

        assertThat(response.getDate()).isEqualTo(deliveryDate);
        assertThat(response.getAm().isAvailable()).isTrue();
        assertThat(response.getAm().getRemaining()).isEqualTo(3);
        assertThat(response.getPm().isAvailable()).isTrue();
        assertThat(response.getPm().getRemaining()).isEqualTo(3);
    }

    @Test
    void getAvailabilityShowsAllSlotsUnavailableWhenDailyCapacityIsReached() {
        User buyer = buyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(3);
        Florist florist = Florist.builder()
                .id(9L)
                .maxDailyCapacity(2)
                .timeSlots("AM,PM")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));
        when(orderRepository.countDistinctActiveOrdersByFloristIdAndDeliveryDate(9L, deliveryDate))
                .thenReturn(2L);

        DeliverySlotAvailabilityResponse response =
                deliverySlotAvailabilityService.getAvailability(buyer, 9L, deliveryDate);

        assertThat(response.getAm().isAvailable()).isFalse();
        assertThat(response.getAm().getRemaining()).isZero();
        assertThat(response.getPm().isAvailable()).isFalse();
        assertThat(response.getPm().getRemaining()).isZero();
    }

    @Test
    void getAvailabilityDisablesSlotsMissingFromFloristProfile() {
        User buyer = buyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(4);
        Florist florist = Florist.builder()
                .id(9L)
                .maxDailyCapacity(3)
                .timeSlots("AM")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));
        when(orderRepository.countDistinctActiveOrdersByFloristIdAndDeliveryDate(9L, deliveryDate))
                .thenReturn(1L);

        DeliverySlotAvailabilityResponse response =
                deliverySlotAvailabilityService.getAvailability(buyer, 9L, deliveryDate);

        assertThat(response.getAm().isAvailable()).isTrue();
        assertThat(response.getAm().getRemaining()).isEqualTo(2);
        assertThat(response.getPm().isAvailable()).isFalse();
        assertThat(response.getPm().getRemaining()).isZero();
    }

    @Test
    void validateOrderSlotRejectsPastDeliveryDate() {
        User buyer = buyer();

        assertThatThrownBy(() -> deliverySlotAvailabilityService.validateOrderSlot(
                buyer,
                9L,
                LocalDate.now().minusDays(1),
                "AM"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Delivery date cannot be in the past");
    }

    @Test
    void validateOrderSlotRejectsFullCapacityFloristDate() {
        User buyer = buyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(2);
        Florist florist = Florist.builder()
                .id(9L)
                .maxDailyCapacity(1)
                .timeSlots("AM,PM")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));
        when(orderRepository.countDistinctActiveOrdersByFloristIdAndDeliveryDate(9L, deliveryDate))
                .thenReturn(1L);

        assertThatThrownBy(() -> deliverySlotAvailabilityService.validateOrderSlot(
                buyer,
                9L,
                deliveryDate,
                "AM"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This florist is fully booked for this date. Please choose another date.");
    }

    @Test
    void validateOrderSlotRejectsUnsupportedFloristTimeSlot() {
        User buyer = buyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(2);
        Florist florist = Florist.builder()
                .id(9L)
                .maxDailyCapacity(3)
                .timeSlots("AM")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));

        assertThatThrownBy(() -> deliverySlotAvailabilityService.validateOrderSlot(
                buyer,
                9L,
                deliveryDate,
                "PM"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Selected delivery time slot is unavailable");
    }


    @Test
    void validateOrderSlotAllowsOrderWhenCapacityRemains() {
        User buyer = buyer();
        LocalDate deliveryDate = LocalDate.now().plusDays(2);
        Florist florist = Florist.builder()
                .id(9L)
                .maxDailyCapacity(2)
                .timeSlots("AM,PM")
                .build();

        when(floristRepository.findById(9L)).thenReturn(Optional.of(florist));
        when(orderRepository.countDistinctActiveOrdersByFloristIdAndDeliveryDate(9L, deliveryDate))
                .thenReturn(1L);

        deliverySlotAvailabilityService.validateOrderSlot(buyer, 9L, deliveryDate, "PM");

        verify(orderRepository).countDistinctActiveOrdersByFloristIdAndDeliveryDate(9L, deliveryDate);
    }

    @Test
    void getAvailabilityRejectsNonBuyerUsers() {
        User floristUser = User.builder().id(4L).role("ROLE_FLORIST").build();

        assertThatThrownBy(() -> deliverySlotAvailabilityService.getAvailability(
                floristUser,
                9L,
                LocalDate.now().plusDays(1)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private User buyer() {
        return User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();
    }
}
