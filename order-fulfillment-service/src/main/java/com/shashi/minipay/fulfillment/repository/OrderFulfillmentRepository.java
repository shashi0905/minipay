package com.shashi.minipay.fulfillment.repository;

import com.shashi.minipay.fulfillment.entity.OrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderFulfillmentRepository extends JpaRepository<OrderFulfillment, UUID> {

    Optional<OrderFulfillment> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}
