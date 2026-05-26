package com.petal.repository;

import com.petal.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByOrderId(Long orderId);
    List<Conversation> findByBuyerId(Long buyerId);
    List<Conversation> findByFloristId(Long floristId);
}
