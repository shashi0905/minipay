package com.shashi.minipay.fulfillment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_fulfillments")
@Getter
@Setter
public class OrderFulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfillmentStatus status;

    @Column(nullable = true)
    private String trackingNumber;

    @Column(nullable = true)
    private String shippingAddress;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = true)
    private Instant shippedAt;

    @Column(nullable = true)
    private Instant deliveredAt;
}
