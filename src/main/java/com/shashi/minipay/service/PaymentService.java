package com.shashi.minipay.service;

import com.shashi.minipay.dto.request.CreatePaymentRequest;
import com.shashi.minipay.dto.response.CreatePaymentResponse;
import com.shashi.minipay.entity.Payment;
import com.shashi.minipay.entity.PaymentStatus;
import com.shashi.minipay.exception.InvalidPaymentStateException;
import com.shashi.minipay.exception.PaymentNotFoundException;
import com.shashi.minipay.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Create payment with idempotency support.
     * If idempotencyKey already exists, return the existing payment.
     * Otherwise, create a new payment in CREATED status.
     */
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        // Check for idempotency - if payment with this key already exists, return it
        var existingPayment = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingPayment.isPresent()) {
            return toResponse(existingPayment.get());
        }

        // Create new payment
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setStatus(PaymentStatus.CREATED);

        Instant now = Instant.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    /**
     * Get payment by payment ID.
     */
    public CreatePaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return toResponse(payment);
    }

    /**
     * Get payment by order ID.
     */
    public CreatePaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderId: " + orderId));
        return toResponse(payment);
    }

    /**
     * Transition payment to PROCESSING status.
     */
    public void transitionToProcessing(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new InvalidPaymentStateException(
                    "Cannot transition payment from " + payment.getStatus() + " to PROCESSING. Expected status: CREATED"
            );
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
    }

    /**
     * Transition payment to SUCCESS status and record provider transaction ID.
     */
    public void transitionToSuccess(UUID paymentId, String providerTransactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException(
                    "Cannot transition payment from " + payment.getStatus() + " to SUCCESS. Expected status: PROCESSING"
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProviderTransactionId(providerTransactionId);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
    }

    /**
     * Transition payment to FAILED status.
     */
    public void transitionToFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException(
                    "Cannot transition payment from " + payment.getStatus() + " to FAILED. Expected status: PROCESSING"
            );
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
    }

    /**
     * Transition payment to CANCELLED status.
     */
    public void transitionToCancelled(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new InvalidPaymentStateException(
                    "Cannot cancel payment with status: " + payment.getStatus() + ". Payments can only be cancelled in CREATED or PROCESSING state."
            );
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
    }

    private CreatePaymentResponse toResponse(Payment payment) {
        return new CreatePaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProviderTransactionId(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
