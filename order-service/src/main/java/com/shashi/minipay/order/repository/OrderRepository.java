package com.shashi.minipay.order.repository;

import com.shashi.minipay.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByUserId(String userId);

    boolean existsByOrderNumber(String orderNumber);
}
