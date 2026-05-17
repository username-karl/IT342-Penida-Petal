package com.petal.service;

import com.petal.dto.BuyerOrderItemResponse;
import com.petal.dto.BuyerOrderResponse;
import com.petal.dto.CreateOrderRequest;
import com.petal.dto.OrderResponse;
import com.petal.dto.SellerOrderItemResponse;
import com.petal.dto.SellerOrderResponse;
import com.petal.dto.SellerOrderStatusRequest;
import com.petal.entity.CartItem;
import com.petal.entity.Florist;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final FloristService floristService;
    private static final Set<String> SELLER_STATUSES = Set.of(
            "PENDING",
            "PREPARING",
            "READY_FOR_PICKUP",
            "COMPLETED",
            "CANCELLED");

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
                .paymentMethod(normalizePaymentMethod(request.getPaymentMethod()))
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
                .paymentMethod(displayPaymentMethod(savedOrder.getPaymentMethod()))
                .totalAmount(savedOrder.getTotalAmount())
                .message("Order placed successfully.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<BuyerOrderResponse> getBuyerOrders(User user) {
        return orderRepository.findByUserOrderByDeliveryDateDescIdDesc(user)
                .stream()
                .map(this::toBuyerOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BuyerOrderResponse getBuyerOrder(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toBuyerOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<SellerOrderResponse> getSellerOrders(User seller) {
        Florist florist = floristService.getOrCreateForUser(seller);
        return orderRepository.findDistinctByItemsProductFloristIdOrderByDeliveryDateAscIdAsc(florist.getId())
                .stream()
                .map(order -> toSellerOrderResponse(order, florist.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SellerOrderResponse getSellerOrder(User seller, Long orderId) {
        Florist florist = floristService.getOrCreateForUser(seller);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for this seller"));

        if (!hasSellerItems(order, florist.getId())) {
            throw new IllegalArgumentException("Order not found for this seller");
        }

        return toSellerOrderResponse(order, florist.getId());
    }

    @Transactional
    public SellerOrderResponse updateSellerOrderStatus(
            User seller,
            Long orderId,
            SellerOrderStatusRequest request) {
        Florist florist = floristService.getOrCreateForUser(seller);
        String status = normalizeStatus(request.getStatus());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for this seller"));

        if (!hasSellerItems(order, florist.getId())) {
            throw new IllegalArgumentException("Order not found for this seller");
        }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return toSellerOrderResponse(savedOrder, florist.getId());
    }

    private SellerOrderResponse toSellerOrderResponse(Order order, Long floristId) {
        List<SellerOrderItemResponse> items = order.getItems().stream()
                .filter(item -> floristId.equals(item.getProduct().getFloristId()))
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toSellerOrderItemResponse)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(SellerOrderItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SellerOrderResponse.builder()
                .id(order.getId())
                .orderNumber("PET-" + String.format("%04d", order.getId()))
                .buyerName(order.getUser().getName())
                .recipientName(order.getRecipientName())
                .recipientAddress(order.getRecipientAddress())
                .cardMessage(order.getCardMessage())
                .deliveryDate(order.getDeliveryDate())
                .timeSlot(order.getTimeSlot())
                .paymentMethod(displayPaymentMethod(order.getPaymentMethod()))
                .status(order.getStatus())
                .sellerSubtotal(subtotal)
                .itemSummary(items.stream()
                        .map(item -> item.getProductName() + " x" + item.getQuantity())
                        .reduce((first, second) -> first + ", " + second)
                        .orElse("No seller items"))
                .items(items)
                .build();
    }

    private BuyerOrderResponse toBuyerOrderResponse(Order order) {
        List<BuyerOrderItemResponse> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toBuyerOrderItemResponse)
                .toList();

        return BuyerOrderResponse.builder()
                .id(order.getId())
                .orderNumber("PET-" + String.format("%04d", order.getId()))
                .recipientName(order.getRecipientName())
                .recipientAddress(order.getRecipientAddress())
                .cardMessage(order.getCardMessage())
                .deliveryDate(order.getDeliveryDate())
                .timeSlot(order.getTimeSlot())
                .paymentMethod(displayPaymentMethod(order.getPaymentMethod()))
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .itemSummary(items.stream()
                        .map(item -> item.getProductName() + " x" + item.getQuantity())
                        .reduce((first, second) -> first + ", " + second)
                        .orElse("No items"))
                .items(items)
                .build();
    }

    private BuyerOrderItemResponse toBuyerOrderItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        Product product = item.getProduct();
        return BuyerOrderItemResponse.builder()
                .productId(product.getId())
                .productName(item.getProductName())
                .imageUrl(product.getImageUrl())
                .floristName(product.getFloristName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(lineTotal)
                .build();
    }

    private SellerOrderItemResponse toSellerOrderItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return SellerOrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(lineTotal)
                .build();
    }

    private boolean hasSellerItems(Order order, Long floristId) {
        return order.getItems().stream()
                .anyMatch(item -> floristId.equals(item.getProduct().getFloristId()));
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!SELLER_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported order status");
        }
        return normalized;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase();
        Set<String> allowed = Set.of("COD", "GCASH", "MAYA", "CARD");
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported payment method");
        }
        return normalized;
    }

    private String displayPaymentMethod(String paymentMethod) {
        return paymentMethod == null || paymentMethod.isBlank() ? "COD" : paymentMethod;
    }
}
