package com.petal.service;

import com.petal.dto.CreateReviewRequest;
import com.petal.dto.ReviewResponse;
import com.petal.dto.ReviewSummaryResponse;
import com.petal.entity.Order;
import com.petal.entity.OrderItem;
import com.petal.entity.Product;
import com.petal.entity.Review;
import com.petal.entity.User;
import com.petal.exception.ForbiddenException;
import com.petal.repository.OrderRepository;
import com.petal.repository.ProductRepository;
import com.petal.repository.ReviewRepository;
import com.petal.repository.FloristRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FloristRepository floristRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, Long orderId, Long productId, CreateReviewRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only review your own orders");
        }

        if (!"DELIVERED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Reviews can only be submitted for delivered orders");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!productInOrder) {
            throw new IllegalArgumentException("The specified product is not part of this order");
        }

        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, orderId, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product for this order");
        }

        com.petal.entity.Florist florist = floristRepository.findById(product.getFloristId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Florist not found"));

        Review review = Review.builder()
                .user(order.getUser())
                .order(order)
                .product(product)
                .florist(florist)
                .productRating(request.getProductRating())
                .floristRating(request.getFloristRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    public ReviewSummaryResponse getProductReviews(Long productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Double avgRating = reviewRepository.getAverageProductRating(productId);
        long totalReviews = reviewRepository.countByProductId(productId);

        Page<Review> reviewPage = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, PageRequest.of(page, size));
        List<ReviewResponse> recentReviews = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ReviewSummaryResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .recentReviews(recentReviews)
                .build();
    }

    public ReviewSummaryResponse getFloristReviews(Long floristId, int page, int size) {
        Double avgRating = reviewRepository.getAverageFloristRating(floristId);
        long totalReviews = reviewRepository.countByFloristId(floristId);

        Page<Review> reviewPage = reviewRepository.findByFloristIdOrderByCreatedAtDesc(floristId, PageRequest.of(page, size));
        List<ReviewResponse> recentReviews = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ReviewSummaryResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .recentReviews(recentReviews)
                .build();
    }

    private ReviewResponse mapToResponse(Review review) {
        User user = review.getUser();
        String reviewerName = user.getName() != null && !user.getName().isEmpty()
                ? user.getName().substring(0, 1).toUpperCase() + "." // Just an initial if we want privacy, or split first name.
                : "Anonymous";
        
        // Better privacy: Just First name and last initial
        if (user.getName() != null && user.getName().contains(" ")) {
            String[] parts = user.getName().split(" ");
            reviewerName = parts[0] + " " + parts[parts.length - 1].substring(0, 1).toUpperCase() + ".";
        } else if (user.getName() != null) {
            reviewerName = user.getName();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .productId(review.getProduct().getId())
                .reviewerName(reviewerName)
                .productRating(review.getProductRating())
                .floristRating(review.getFloristRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
