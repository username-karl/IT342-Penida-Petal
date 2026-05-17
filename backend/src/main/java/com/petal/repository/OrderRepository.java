package com.petal.repository;

import com.petal.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
            select distinct o from Order o
            join fetch o.items i
            join fetch i.product p
            join fetch o.user u
            where p.floristId = :floristId
            order by o.deliveryDate asc, o.id asc
            """)
    List<Order> findDistinctByItemsProductFloristIdOrderByDeliveryDateAscIdAsc(
            @Param("floristId") Long floristId);
}
