package com.petal.repository;

import com.petal.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndOrderIdAndProductId(Long userId, Long orderId, Long productId);

    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Page<Review> findByFloristIdOrderByCreatedAtDesc(Long floristId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    @Query("SELECT AVG(r.productRating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageProductRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.florist.id = :floristId")
    long countByFloristId(@Param("floristId") Long floristId);

    @Query("SELECT AVG(r.floristRating) FROM Review r WHERE r.florist.id = :floristId")
    Double getAverageFloristRating(@Param("floristId") Long floristId);
}
