package com.petal.repository;

import com.petal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select distinct p from Product p join p.moodTags tag where lower(tag) = lower(:mood)")
    List<Product> findByMood(@Param("mood") String mood);

    List<Product> findByFloristIdOrderByIdDesc(Long floristId);

    boolean existsByName(String name);
}
