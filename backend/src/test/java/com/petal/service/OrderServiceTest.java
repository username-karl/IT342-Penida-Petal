package com.petal.service;

import com.petal.dto.SellerOrderResponse;
import com.petal.dto.SellerOrderStatusRequest;
import com.petal.entity.Florist;
import com.petal.entity.Order;
import com.petal.entity.OrderItem;
import com.petal.entity.Product;
import com.petal.entity.User;
import com.petal.repository.CartItemRepository;
import com.petal.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                SellerOrderStatusRequest.builder().status("PREPARING").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found for this seller");
    }

    @Test
    void updateSellerOrderStatusSavesAllowedStatus() {
        User seller = seller();
        Florist florist = Florist.builder().id(9L).user(seller).storeName("Karl's Studio").build();
        Order order = orderWithItems(seller, "PENDING");

        Mockito.when(floristService.getOrCreateForUser(seller)).thenReturn(florist);
        Mockito.when(orderRepository.findById(25L)).thenReturn(Optional.of(order));
        Mockito.when(orderRepository.save(order)).thenReturn(order);

        SellerOrderResponse response = orderService.updateSellerOrderStatus(
                seller,
                25L,
                SellerOrderStatusRequest.builder().status("PREPARING").build());

        assertThat(response.getStatus()).isEqualTo("PREPARING");
        verify(orderRepository).save(order);
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
}
