package com.petal.repository;

import com.petal.entity.Florist;
import com.petal.entity.Mood;
import com.petal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByFlorist(Florist florist);

    List<Product> findByFloristId(Long floristId);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.moodTags m WHERE m = :mood AND p.inStock = true")
    List<Product> findByMoodTag(@Param("mood") Mood mood);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.moodTags m WHERE m = :mood AND p.florist = :florist")
    List<Product> findByMoodTagAndFlorist(@Param("mood") Mood mood, @Param("florist") Florist florist);

    List<Product> findByInStockTrue();
}
