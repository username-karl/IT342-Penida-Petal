package com.petal.service;

import com.petal.dto.CreateOrderRequest;
import com.petal.dto.OrderResponse;
import com.petal.entity.CartItem;
import com.petal.entity.Order;
import com.petal.entity.OrderItem;
import com.petal.entity.Product;
import com.petal.entity.User;
import com.petal.repository.CartItemRepository;
import com.petal.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(User user, CreateOrderRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserOrderByIdAsc(user);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .recipientAddress(request.getRecipientAddress())
                .cardMessage(request.getCardMessage())
                .deliveryDate(request.getDeliveryDate())
                .timeSlot(request.getTimeSlot())
                .status("PENDING")
                .totalAmount(totalAmount)
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .status(savedOrder.getStatus())
                .deliveryDate(savedOrder.getDeliveryDate())
                .timeSlot(savedOrder.getTimeSlot())
                .totalAmount(savedOrder.getTotalAmount())
                .message("Order placed successfully.")
                .build();
    }
}
