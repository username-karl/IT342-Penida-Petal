package com.petal.repository;

import com.petal.entity.BuyerNotification;
import com.petal.entity.SavedDate;
import com.petal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerNotificationRepository extends JpaRepository<BuyerNotification, Long> {
    List<BuyerNotification> findByBuyerOrderByCreatedAtDescIdDesc(User buyer);

    List<BuyerNotification> findByBuyerAndReadAtIsNullOrderByCreatedAtDescIdDesc(User buyer);

    Optional<BuyerNotification> findByIdAndBuyer(Long id, User buyer);

    boolean existsByBuyerAndTypeAndSavedDateAndNotificationYear(
            User buyer,
            String type,
            SavedDate savedDate,
            Integer notificationYear);
}
