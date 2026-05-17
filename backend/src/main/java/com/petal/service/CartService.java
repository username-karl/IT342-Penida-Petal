package com.petal.service;

import com.petal.dto.CartItemResponse;
import com.petal.dto.CartResponse;
import com.petal.entity.CartItem;
import com.petal.entity.Product;
import com.petal.entity.User;
import com.petal.repository.CartItemRepository;
import com.petal.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(User user) {
        List<CartItemResponse> items = cartItemRepository.findByUserOrderByIdAsc(user).stream()
                .map(this::toResponse)
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .subtotal(subtotal)
                .build();
    }

    @Transactional
    public CartItemResponse addItem(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.isInStock()) {
            throw new IllegalArgumentException("Product is out of stock");
        }

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    return existingItem;
                })
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(quantity)
                        .build());

        return toResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponse updateItem(User user, Long itemId, int quantity) {
        CartItem cartItem = cartItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        cartItem.setQuantity(quantity);
        return toResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeItem(User user, Long itemId) {
        CartItem cartItem = cartItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    private CartItemResponse toResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal unitPrice = product.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .floristName(product.getFloristName())
                .unitPrice(unitPrice)
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
}
