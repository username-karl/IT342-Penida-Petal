package com.petal.repository;

import com.petal.entity.Florist;
import com.petal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloristRepository extends JpaRepository<Florist, Long> {
    Optional<Florist> findByUser(User user);
    Optional<Florist> findByUserId(Long userId);
    boolean existsByUser(User user);
}
