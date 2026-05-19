package com.petal.repository;

import com.petal.entity.DeliveryAddress;
import com.petal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    List<DeliveryAddress> findByUserOrderByDefaultAddressDescIdAsc(User user);

    Optional<DeliveryAddress> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("update DeliveryAddress address set address.defaultAddress = false where address.user = :user")
    void clearDefaultAddressForUser(User user);
}
