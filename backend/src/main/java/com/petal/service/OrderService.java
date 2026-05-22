package com.petal.service;

import com.petal.dto.BuyerOrderItemResponse;
import com.petal.dto.BuyerOrderResponse;
import com.petal.dto.CreateOrderRequest;
import com.petal.dto.OrderResponse;
import com.petal.dto.SellerOrderItemResponse;
import com.petal.dto.SellerOrderResponse;
import com.petal.dto.SellerOrderStatusRequest;
import com.petal.dto.ShippingInfoResponse;
import com.petal.dto.ShippingUpdateRequest;
import com.petal.dto.TrackingEventResponse;
import com.petal.entity.CartItem;
import com.petal.entity.Florist;
import com.petal.entity.Order;
import com.petal.entity.OrderItem;
import com.petal.entity.Product;
import com.petal.entity.TrackingEvent;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.CartItemRepository;
import com.petal.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final FloristService floristService;
    private final OrderImageStorageService orderImageStorageService;
    private final DeliverySlotAvailabilityService deliverySlotAvailabilityService;
    private static final Set<String> SELLER_STATUSES = Set.of(
            "PENDING",
            "ACCEPTED",
            "ARRANGING",
            "READY_FOR_PICKUP",
            "OUT_FOR_DELIVERY",
            "DELIVERED",
            "CANCELLED");
    private static final Map<String, String> NEXT_STATUSES = Map.of(
            "PENDING", "ACCEPTED",
            "ACCEPTED", "ARRANGING",
            "ARRANGING", "READY_FOR_PICKUP",
            "READY_FOR_PICKUP", "OUT_FOR_DELIVERY",
            "OUT_FOR_DELIVERY", "DELIVERED");
    private static final Set<String> CANCELLABLE_STATUSES = Set.of("PENDING", "ACCEPTED", "ARRANGING");
    private static final Set<String> SHIPPING_DETAIL_STATUSES = Set.of("OUT_FOR_DELIVERY", "DELIVERED");
    private static final Set<String> FULFILLMENT_PHOTO_STATUSES = Set.of("ARRANGING", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY", "DELIVERED");
    private static final Set<String> PHOTO_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final long MAX_PHOTO_BYTES = 5L * 1024L * 1024L;

    @Transactional
    public OrderResponse createOrder(User user, CreateOrderRequest request) {
        requireBuyer(user);
        List<CartItem> cartItems = cartItemRepository.findByUserOrderByIdAsc(user);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        Long floristId = resolveSingleFloristId(cartItems);
        deliverySlotAvailabilityService.validateOrderSlot(
                user,
                floristId,
                request.getDeliveryDate(),
                request.getTimeSlot());

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
        addTrackingEvent(order, "PENDING", null, LocalDateTime.now());

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
        requireBuyer(user);
        return orderRepository.findByUserOrderByDeliveryDateDescIdDesc(user)
                .stream()
                .map(this::toBuyerOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BuyerOrderResponse getBuyerOrder(User user, Long orderId) {
        requireBuyer(user);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseGet(() -> {
                    if (orderRepository.findById(orderId).isPresent()) {
                        throw new ForbiddenException("Order not found");
                    }
                    throw new IllegalArgumentException("Order not found");
                });
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
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!hasSellerItems(order, florist.getId())) {
            throw new ForbiddenException("Order not found for this seller");
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
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!hasSellerItems(order, florist.getId())) {
            throw new ForbiddenException("Order not found for this seller");
        }

        ensureSingleSellerOrder(order, florist.getId());
        if (SHIPPING_DETAIL_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Shipping updates require tracking details");
        }
        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);
        order.setLatestShippingStatus(displayStatus(status));
        addTrackingEvent(order, status, null, LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        return toSellerOrderResponse(savedOrder, florist.getId());
    }

    @Transactional
    public SellerOrderResponse updateSellerShipping(
            User seller,
            Long orderId,
            ShippingUpdateRequest request) {
        Florist florist = floristService.getOrCreateForUser(seller);
        String status = normalizeStatus(request.getDeliveryStatus());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for this seller"));

        if (!hasSellerItems(order, florist.getId())) {
            throw new IllegalArgumentException("Order not found for this seller");
        }

        ensureSingleSellerOrder(order, florist.getId());
        validateStatusTransition(order.getStatus(), status);
        order.setCourierName(request.getCourierName());
        order.setTrackingNumber(request.getTrackingNumber());
        order.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        order.setStatus(status);
        order.setLatestShippingStatus(displayStatus(status));
        addTrackingEvent(
                order,
                status,
                request.getTrackingMessage(),
                request.getTimestamp() == null ? LocalDateTime.now() : request.getTimestamp());

        Order savedOrder = orderRepository.save(order);
        return toSellerOrderResponse(savedOrder, florist.getId());
    }

    @Transactional
    public SellerOrderResponse uploadFulfillmentPhoto(User seller, Long orderId, MultipartFile file) {
        validatePhotoFile(file);
        Florist florist = requireUploadFlorist(seller);
        Order order = sellerOwnedSingleOrder(orderId, florist.getId());
        String status = normalizeStatus(order.getStatus());
        if (!FULFILLMENT_PHOTO_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Fulfillment photo is allowed once the order is being arranged");
        }
        String imageUrl = orderImageStorageService.store(file, "fulfillment");
        order.setFulfillmentImageUrl(imageUrl);
        Order savedOrder = orderRepository.save(order);
        return toSellerOrderResponse(savedOrder, florist.getId());
    }

    @Transactional
    public SellerOrderResponse uploadProofPhoto(User seller, Long orderId, MultipartFile file) {
        validatePhotoFile(file);
        Florist florist = requireUploadFlorist(seller);
        Order order = sellerOwnedSingleOrder(orderId, florist.getId());
        String status = normalizeStatus(order.getStatus());
        if (!"DELIVERED".equals(status)) {
            throw new IllegalArgumentException("Proof of delivery photo is allowed after the order is delivered");
        }
        String imageUrl = orderImageStorageService.store(file, "proof");
        order.setProofImageUrl(imageUrl);
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
                .fulfillmentImageUrl(order.getFulfillmentImageUrl())
                .proofImageUrl(order.getProofImageUrl())
                .shipping(toShippingInfoResponse(order))
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
                .fulfillmentImageUrl(order.getFulfillmentImageUrl())
                .proofImageUrl(order.getProofImageUrl())
                .shipping(toShippingInfoResponse(order))
                .items(items)
                .build();
    }

    private ShippingInfoResponse toShippingInfoResponse(Order order) {
        List<TrackingEventResponse> events = order.getTrackingEvents().stream()
                .sorted(Comparator.comparing(TrackingEvent::getTimestamp, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(event -> workflowRank(event.getStatus()))
                        .reversed())
                .map(event -> TrackingEventResponse.builder()
                        .id(event.getId())
                        .status(event.getStatus())
                        .description(event.getDescription())
                        .timestamp(event.getTimestamp())
                        .build())
                .toList();
        String currentStatus = order.getStatus() == null || order.getStatus().isBlank()
                ? "PENDING"
                : normalizeStatus(order.getStatus());
        String latestStatus = order.getLatestShippingStatus() == null || order.getLatestShippingStatus().isBlank()
                ? displayStatus(currentStatus)
                : order.getLatestShippingStatus();
        if (events.isEmpty()) {
            events = List.of(TrackingEventResponse.builder()
                    .status(latestStatus)
                    .description(defaultTrackingMessage(currentStatus))
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        String trackingNumber = order.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = "PETAL-" + String.format("%04d", order.getId());
        }

        return ShippingInfoResponse.builder()
                .courierName(displayCourier(order.getCourierName()))
                .trackingNumber(trackingNumber)
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate() == null
                        ? order.getDeliveryDate()
                        : order.getEstimatedDeliveryDate())
                .latestStatus(latestStatus)
                .fulfillmentImageUrl(order.getFulfillmentImageUrl())
                .proofImageUrl(order.getProofImageUrl())
                .events(events)
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

    private Long resolveSingleFloristId(List<CartItem> cartItems) {
        Long floristId = cartItems.get(0).getProduct().getFloristId();
        boolean hasOtherFlorists = cartItems.stream()
                .anyMatch(item -> !floristId.equals(item.getProduct().getFloristId()));
        if (hasOtherFlorists) {
            throw new IllegalArgumentException("Checkout supports one florist per order");
        }
        return floristId;
    }

    private void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!PHOTO_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, or WEBP images are allowed");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Photo must be 5MB or smaller");
        }
    }

    private Florist requireUploadFlorist(User seller) {
        if (seller == null || !"ROLE_FLORIST".equals(seller.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Florist access is required");
        }
        return floristService.getOrCreateForUser(seller);
    }

    private Order sellerOwnedSingleOrder(Long orderId, Long floristId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Order not found for this seller"));
        if (!hasSellerItems(order, floristId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order not found for this seller");
        }
        ensureSingleSellerOrder(order, floristId);
        return order;
    }

    private void ensureSingleSellerOrder(Order order, Long floristId) {
        boolean hasOtherSellerItems = order.getItems().stream()
                .anyMatch(item -> !floristId.equals(item.getProduct().getFloristId()));
        if (hasOtherSellerItems) {
            throw new ForbiddenException("Order cannot be updated from seller view");
        }
    }

    private void requireBuyer(User user) {
        if (user == null || !"ROLE_BUYER".equals(user.getRole())) {
            throw new ForbiddenException("Buyer access is required");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if ("PREPARING".equals(normalized)) {
            normalized = "ARRANGING";
        }
        if ("SHIPPED".equals(normalized)) {
            normalized = "OUT_FOR_DELIVERY";
        }
        if ("COMPLETED".equals(normalized)) {
            normalized = "DELIVERED";
        }
        if (!SELLER_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported order status");
        }
        return normalized;
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        String current = normalizeStatus(currentStatus);
        if ("PENDING".equals(nextStatus)) {
            throw new IllegalArgumentException("Invalid order status transition");
        }
        if ("CANCELLED".equals(nextStatus)) {
            if (CANCELLABLE_STATUSES.contains(current)) {
                return;
            }
            throw new IllegalArgumentException("Invalid order status transition");
        }
        if (!nextStatus.equals(NEXT_STATUSES.get(current))) {
            throw new IllegalArgumentException("Invalid order status transition");
        }
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

    private String displayCourier(String courierName) {
        return courierName == null || courierName.isBlank() ? "Petal Local Delivery" : courierName;
    }

    private String displayStatus(String status) {
        if (status == null || status.isBlank()) return "Pending";
        return switch (status) {
            case "PENDING" -> "Pending";
            case "ACCEPTED" -> "Accepted";
            case "ARRANGING", "PREPARING" -> "Arranging";
            case "READY_FOR_PICKUP" -> "Ready for pickup";
            case "SHIPPED", "OUT_FOR_DELIVERY" -> "Out for delivery";
            case "DELIVERED", "COMPLETED" -> "Delivered";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }

    private void addTrackingEvent(Order order, String status, String message, LocalDateTime timestamp) {
        order.getTrackingEvents().add(TrackingEvent.builder()
                .order(order)
                .status(displayStatus(status))
                .description(resolveTrackingMessage(status, message))
                .timestamp(timestamp)
                .build());
    }

    private String resolveTrackingMessage(String status, String message) {
        if (message != null && !message.isBlank()) {
            return message.trim();
        }
        return defaultTrackingMessage(status);
    }

    private String defaultTrackingMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Your order has been received and is waiting for florist confirmation.";
            case "ACCEPTED" -> "The florist has accepted your order.";
            case "ARRANGING", "PREPARING" -> "The florist is preparing your bouquet.";
            case "READY_FOR_PICKUP" -> "Your bouquet is ready for courier pickup.";
            case "OUT_FOR_DELIVERY" -> "Your bouquet is on the way to the recipient.";
            case "DELIVERED", "COMPLETED" -> "Your bouquet has been successfully delivered to the recipient.";
            case "CANCELLED" -> "This order has been cancelled.";
            default -> "Tracking information has been updated.";
        };
    }

    private int workflowRank(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase().replace(" ", "_");
        return switch (normalized) {
            case "PENDING" -> 1;
            case "ACCEPTED" -> 2;
            case "ARRANGING", "PREPARING" -> 3;
            case "READY_FOR_PICKUP" -> 4;
            case "OUT_FOR_DELIVERY", "SHIPPED" -> 5;
            case "DELIVERED", "COMPLETED" -> 6;
            case "CANCELLED" -> 7;
            default -> 0;
        };
    }
}
