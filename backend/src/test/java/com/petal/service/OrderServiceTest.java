package com.petal.service;

import com.petal.dto.BuyerOrderResponse;
import com.petal.dto.CreateOrderRequest;
import com.petal.dto.SellerOrderResponse;
import com.petal.dto.ShippingUpdateRequest;
import com.petal.dto.SellerOrderStatusRequest;
import com.petal.entity.CartItem;
import com.petal.entity.Florist;
import com.petal.entity.Order;
import com.petal.entity.OrderItem;
import com.petal.entity.Product;
import com.petal.entity.TrackingEvent;
import com.petal.entity.User;
import com.petal.repository.CartItemRepository;
import com.petal.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FloristService floristService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderStartsBuyerTimelineWithPendingTrackingEvent() {
        User buyer = User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();
        Product product = Product.builder()
                .id(7L)
                .name("Aurora Hydrangea")
                .price(new BigDecimal("1500.00"))
                .floristId(9L)
                .floristName("Karl's Studio")
                .build();
        CartItem cartItem = CartItem.builder()
                .id(12L)
                .user(buyer)
                .product(product)
                .quantity(1)
                .build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .recipientName("Lara Santos")
                .recipientAddress("Cebu Business Park")
                .deliveryDate(LocalDate.of(2026, 5, 20))
                .timeSlot("AM")
                .paymentMethod("GCASH")
                .build();

        Mockito.when(cartItemRepository.findByUserOrderByIdAsc(buyer)).thenReturn(List.of(cartItem));
        Mockito.when(orderRepository.save(Mockito.any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(25L);
            return savedOrder;
        });

        orderService.createOrder(buyer, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        Mockito.verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getTrackingEvents()).hasSize(1);
        assertThat(savedOrder.getTrackingEvents().get(0).getStatus()).isEqualTo("Pending");
        assertThat(savedOrder.getTrackingEvents().get(0).getDescription())
                .isEqualTo("Your order has been received and is waiting for florist confirmation.");
    }

    @Test
    void getBuyerOrdersReturnsOnlyCurrentUsersOrderHistory() {
        User buyer = User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();
        Order order = orderWithItems(buyer, "READY_FOR_PICKUP");

        Mockito.when(orderRepository.findByUserOrderByDeliveryDateDescIdDesc(buyer))
                .thenReturn(List.of(order));

        List<BuyerOrderResponse> orders = orderService.getBuyerOrders(buyer);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderNumber()).isEqualTo("PET-0025");
        assertThat(orders.get(0).getStatus()).isEqualTo("READY_FOR_PICKUP");
        assertThat(orders.get(0).getPaymentMethod()).isEqualTo("GCASH");
        assertThat(orders.get(0).getTotalAmount()).isEqualByComparingTo("3800.00");
        assertThat(orders.get(0).getItemSummary()).isEqualTo("Aurora Hydrangea x2, Other Arrangement x1");
        assertThat(orders.get(0).getItems()).hasSize(2);
    }

    @Test
    void getBuyerOrderRejectsOrdersOwnedByAnotherBuyer() {
        User buyer = User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();

        Mockito.when(orderRepository.findByIdAndUser(25L, buyer))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getBuyerOrder(buyer, 25L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found");
    }

    @Test
    void getBuyerOrderReturnsCurrentUsersOrderDetail() {
        User buyer = User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();
        Order order = orderWithItems(buyer, "READY_FOR_PICKUP");
        order.setCourierName("Petal Cebu Rider");
        order.setTrackingNumber("PETAL-0025-RIDER");
        order.setEstimatedDeliveryDate(LocalDate.of(2026, 5, 19));
        order.setLatestShippingStatus("Out for delivery");
        order.getTrackingEvents().add(TrackingEvent.builder()
                .id(7L)
                .order(order)
                .status("Out for delivery")
                .description("Your bouquet is on the way to the recipient.")
                .timestamp(LocalDateTime.of(2026, 5, 19, 12, 10))
                .build());

        Mockito.when(orderRepository.findByIdAndUser(25L, buyer))
                .thenReturn(Optional.of(order));

        BuyerOrderResponse response = orderService.getBuyerOrder(buyer, 25L);

        assertThat(response.getOrderNumber()).isEqualTo("PET-0025");
        assertThat(response.getRecipientAddress()).isEqualTo("Cebu Business Park");
        assertThat(response.getPaymentMethod()).isEqualTo("GCASH");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getShipping().getCourierName()).isEqualTo("Petal Cebu Rider");
        assertThat(response.getShipping().getEvents()).hasSize(1);
    }

    @Test
    void getBuyerOrderSortsTrackingEventsByWorkflowWhenTimestampsTie() {
        User buyer = User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();
        Order order = orderWithItems(buyer, "OUT_FOR_DELIVERY");
        LocalDateTime sameMinute = LocalDateTime.of(2026, 5, 19, 22, 7);
        order.getTrackingEvents().add(TrackingEvent.builder()
                .id(8L)
                .order(order)
                .status("Accepted")
                .description("The florist has accepted your order.")
                .timestamp(sameMinute)
                .build());
        order.getTrackingEvents().add(TrackingEvent.builder()
                .id(9L)
                .order(order)
                .status("Arranging")
                .description("The florist is preparing your bouquet.")
                .timestamp(sameMinute)
                .build());
        order.getTrackingEvents().add(TrackingEvent.builder()
                .id(10L)
                .order(order)
                .status("Ready for pickup")
                .description("Your bouquet is ready for courier pickup.")
                .timestamp(sameMinute)
                .build());
        order.getTrackingEvents().add(TrackingEvent.builder()
                .id(11L)
                .order(order)
                .status("Out for delivery")
                .description("Your bouquet is on the way to the recipient.")
                .timestamp(sameMinute)
                .build());
        order.getTrackingEvents().add(TrackingEvent.builder()
                .id(7L)
                .order(order)
                .status("Pending")
                .description("Your order has been received and is waiting for florist confirmation.")
                .timestamp(LocalDateTime.of(2026, 5, 19, 22, 6))
                .build());

        Mockito.when(orderRepository.findByIdAndUser(25L, buyer))
                .thenReturn(Optional.of(order));

        BuyerOrderResponse response = orderService.getBuyerOrder(buyer, 25L);

        assertThat(response.getShipping().getEvents())
                .extracting("status")
                .containsExactly("Out for delivery", "Ready for pickup", "Arranging", "Accepted", "Pending");
    }

    @Test
    void getBuyerOrderUsesCurrentStatusAsFallbackTrackingEventWhenEventsAreMissing() {
        User buyer = User.builder().id(2L).name("Mikaela Santos").role("ROLE_BUYER").build();
        Order order = orderWithItems(buyer, "OUT_FOR_DELIVERY");
        order.setLatestShippingStatus("Out for delivery");

        Mockito.when(orderRepository.findByIdAndUser(25L, buyer))
                .thenReturn(Optional.of(order));

        BuyerOrderResponse response = orderService.getBuyerOrder(buyer, 25L);

        assertThat(response.getShipping().getEvents()).hasSize(1);
        assertThat(response.getShipping().getEvents().get(0).getStatus()).isEqualTo("Out for delivery");
        assertThat(response.getShipping().getEvents().get(0).getDescription())
                .isEqualTo("Your bouquet is on the way to the recipient.");
    }

    @Test
    void getSellerOrdersReturnsOnlyItemsOwnedByTheAuthenticatedFlorist() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = orderWithItems(seller, "PENDING");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findDistinctByItemsProductFloristIdOrderByDeliveryDateAscIdAsc(9L))
                .thenReturn(List.of(order));

        List<SellerOrderResponse> orders = orderService.getSellerOrders(seller);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderNumber()).isEqualTo("PET-0025");
        assertThat(orders.get(0).getSellerSubtotal()).isEqualByComparingTo("3000.00");
        assertThat(orders.get(0).getPaymentMethod()).isEqualTo("GCASH");
        assertThat(orders.get(0).getItemSummary()).isEqualTo("Aurora Hydrangea x2");
        assertThat(orders.get(0).getItems()).hasSize(1);
    }

    @Test
    void updateSellerOrderStatusRejectsOrdersWithoutSellerOwnedItems() {
        User seller = seller();
        Florist florist = Florist.builder().id(99L).user(seller).storeName("Other Studio").build();

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(orderWithItems(seller, "PENDING")));

        assertThatThrownBy(() -> orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("ACCEPTED").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found for this seller");
    }

    @Test
    void updateSellerOrderStatusAdvancesOnlyToNextValidStatus() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "PENDING");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order)).thenReturn(order);

        SellerOrderResponse response = orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("ACCEPTED").build());

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        verify(orderRepository).save(order);
    }

    @Test
    void updateSellerOrderStatusAddsTrackingEventForBuyerTimeline() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "PENDING");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order)).thenReturn(order);

        SellerOrderResponse response = orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("ACCEPTED").build());

        assertThat(order.getTrackingEvents()).hasSize(1);
        assertThat(order.getTrackingEvents().get(0).getStatus()).isEqualTo("Accepted");
        assertThat(order.getTrackingEvents().get(0).getDescription())
                .isEqualTo("The florist has accepted your order.");
        assertThat(response.getShipping().getEvents()).hasSize(1);
        assertThat(response.getShipping().getEvents().get(0).getDescription())
                .isEqualTo("The florist has accepted your order.");
    }

    @Test
    void updateSellerOrderStatusRejectsInvalidStatusJump() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "ACCEPTED");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("READY_FOR_PICKUP").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid order status transition");
    }

    @Test
    void updateSellerOrderStatusRejectsMixedSellerOrder() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = orderWithItems(seller, "PENDING");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("ACCEPTED").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order cannot be updated from seller view");
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));
    }

    @Test
    void updateSellerOrderStatusRequiresShippingEndpointForDeliveryStatuses() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "READY_FOR_PICKUP");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("OUT_FOR_DELIVERY").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shipping updates require tracking details");
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));
    }

    @Test
    void getSellerOrderReturnsOnlySellerOwnedItems() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = orderWithItems(seller, "PENDING");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));

        SellerOrderResponse response = orderService.getSellerOrder(seller, 25L);

        assertThat(response.getOrderNumber()).isEqualTo("PET-0025");
        assertThat(response.getSellerSubtotal()).isEqualByComparingTo("3000.00");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getRecipientAddress()).isEqualTo("Cebu Business Park");
    }

    @Test
    void updateSellerShippingAddsTrackingEventAndLatestStatus() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "READY_FOR_PICKUP");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order)).thenReturn(order);

        SellerOrderResponse response = orderService.updateSellerShipping(
                seller,
                25L,
                ShippingUpdateRequest.builder()
                        .courierName("Petal Cebu Rider")
                        .trackingNumber("PETAL-0025-RIDER")
                        .deliveryStatus("OUT_FOR_DELIVERY")
                        .trackingMessage("Your bouquet is on the way to the recipient.")
                        .timestamp(LocalDateTime.of(2026, 5, 19, 12, 10))
                        .build());

        assertThat(response.getStatus()).isEqualTo("OUT_FOR_DELIVERY");
        assertThat(response.getShipping().getLatestStatus()).isEqualTo("Out for delivery");
        assertThat(order.getTrackingEvents()).hasSize(1);
        verify(orderRepository).save(order);
    }

    @Test
    void updateSellerShippingUsesDefaultMessageWhenTrackingMessageIsBlank() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "OUT_FOR_DELIVERY");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order)).thenReturn(order);

        orderService.updateSellerShipping(
                seller,
                25L,
                ShippingUpdateRequest.builder()
                        .courierName("Petal Cebu Rider")
                        .trackingNumber("PETAL-0025-RIDER")
                        .deliveryStatus("DELIVERED")
                        .trackingMessage(" ")
                        .timestamp(LocalDateTime.of(2026, 5, 19, 12, 45))
                        .build());

        assertThat(order.getTrackingEvents()).hasSize(1);
        assertThat(order.getTrackingEvents().get(0).getDescription())
                .isEqualTo("Your bouquet has been successfully delivered to the recipient.");
    }

    @Test
    void updateSellerShippingRejectsSkippingWorkflowSteps() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = singleSellerOrderWithItems(seller, "ACCEPTED");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateSellerShipping(
                seller,
                25L,
                ShippingUpdateRequest.builder()
                        .courierName("Petal Cebu Rider")
                        .trackingNumber("PETAL-0025-RIDER")
                        .deliveryStatus("OUT_FOR_DELIVERY")
                        .trackingMessage("Skipping ahead")
                        .timestamp(LocalDateTime.of(2026, 5, 19, 12, 10))
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid order status transition");
    }

    @Test
    void updateSellerShippingRejectsMixedSellerOrder() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = orderWithItems(seller, "READY_FOR_PICKUP");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateSellerShipping(
                seller,
                25L,
                ShippingUpdateRequest.builder()
                        .courierName("Petal Cebu Rider")
                        .trackingNumber("PETAL-0025-RIDER")
                        .deliveryStatus("OUT_FOR_DELIVERY")
                        .timestamp(LocalDateTime.of(2026, 5, 19, 12, 10))
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order cannot be updated from seller view");
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));
    }

    private User seller() {
        return User.builder()
                .id(4L)
                .name("Karl")
                .email("karl@petal.test")
                .role("ROLE_FLORIST")
                .build();
    }

    private Order orderWithItems(User seller, String status) {
        Product sellerProduct = Product.builder()
                .id(7L)
                .name("Aurora Hydrangea")
                .price(new BigDecimal("1500.00"))
                .floristId(9L)
                .floristName("Karl's Studio")
                .build();
        Product otherProduct = Product.builder()
                .id(8L)
                .name("Other Arrangement")
                .price(new BigDecimal("800.00"))
                .floristId(10L)
                .floristName("Other Studio")
                .build();
        Order order = Order.builder()
                .id(25L)
                .user(User.builder().id(2L).name("Mikaela Santos").build())
                .recipientName("Lara Santos")
                .recipientAddress("Cebu Business Park")
                .deliveryDate(LocalDate.of(2026, 5, 18))
                .timeSlot("AM")
                .status(status)
                .paymentMethod("GCASH")
                .totalAmount(new BigDecimal("3800.00"))
                .build();
        order.getItems().add(OrderItem.builder()
                .id(1L)
                .order(order)
                .product(sellerProduct)
                .productName("Aurora Hydrangea")
                .unitPrice(new BigDecimal("1500.00"))
                .quantity(2)
                .build());
        order.getItems().add(OrderItem.builder()
                .id(2L)
                .order(order)
                .product(otherProduct)
                .productName("Other Arrangement")
                .unitPrice(new BigDecimal("800.00"))
                .quantity(1)
                .build());
        return order;
    }

    private Order singleSellerOrderWithItems(User seller, String status) {
        Order order = orderWithItems(seller, status);
        order.getItems().removeIf(item -> !Long.valueOf(9L).equals(item.getProduct().getFloristId()));
        order.setTotalAmount(new BigDecimal("3000.00"));
        return order;
    }
}
