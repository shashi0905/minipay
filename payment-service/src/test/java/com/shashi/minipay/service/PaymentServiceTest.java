package com.shashi.minipay.service;

import com.shashi.minipay.dto.request.CreatePaymentRequest;
import com.shashi.minipay.dto.response.CreatePaymentResponse;
import com.shashi.minipay.entity.Payment;
import com.shashi.minipay.entity.PaymentStatus;
import com.shashi.minipay.exception.InvalidPaymentStateException;
import com.shashi.minipay.exception.PaymentNotFoundException;
import com.shashi.minipay.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePaymentRequest request;
    private Payment payment;

    @BeforeEach
    void setUp() {
        request = new CreatePaymentRequest(
                "ORD-123",
                "user-123",
                new BigDecimal("100.00"),
                "INR",
                "idempotency-key-123"
        );

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId("ORD-123");
        payment.setUserId("user-123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("INR");
        payment.setIdempotencyKey("idempotency-key-123");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
    }

    @Test
    void createPayment_ShouldCreateNewPayment_WhenIdempotencyKeyNotExists() {
        // Given
        when(paymentRepository.findByIdempotencyKey("idempotency-key-123")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        CreatePaymentResponse response = paymentService.createPayment(request);

        // Then
        assertNotNull(response);
        assertEquals("ORD-123", response.orderId());
        assertEquals("user-123", response.userId());
        assertEquals(PaymentStatus.CREATED, response.status());
        verify(paymentRepository, times(1)).findByIdempotencyKey("idempotency-key-123");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_ShouldReturnExistingPayment_WhenIdempotencyKeyExists() {
        // Given
        when(paymentRepository.findByIdempotencyKey("idempotency-key-123")).thenReturn(Optional.of(payment));

        // When
        CreatePaymentResponse response = paymentService.createPayment(request);

        // Then
        assertNotNull(response);
        assertEquals(payment.getId(), response.id());
        assertEquals("ORD-123", response.orderId());
        verify(paymentRepository, times(1)).findByIdempotencyKey("idempotency-key-123");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenExists() {
        // Given
        UUID paymentId = payment.getId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        CreatePaymentResponse response = paymentService.getPaymentById(paymentId);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.id());
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenNotExists() {
        // Given
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment_WhenExists() {
        // Given
        String orderId = "ORD-123";
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // When
        CreatePaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        // Then
        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        verify(paymentRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void getPaymentByOrderId_ShouldThrowException_WhenNotExists() {
        // Given
        String orderId = "ORD-999";
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentByOrderId(orderId));
        verify(paymentRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void transitionToProcessing_ShouldSucceed_WhenPaymentInCreatedState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.CREATED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.transitionToProcessing(paymentId);

        // Then
        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void transitionToProcessing_ShouldThrowException_WhenPaymentNotInCreatedState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.PROCESSING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(InvalidPaymentStateException.class, () -> paymentService.transitionToProcessing(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void transitionToSuccess_ShouldSucceed_WhenPaymentInProcessingState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.PROCESSING);
        String providerTxId = "provider-tx-123";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.transitionToSuccess(paymentId, providerTxId);

        // Then
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(providerTxId, payment.getProviderTransactionId());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void transitionToSuccess_ShouldThrowException_WhenPaymentNotInProcessingState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.CREATED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(InvalidPaymentStateException.class, () -> paymentService.transitionToSuccess(paymentId, "provider-tx-123"));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void transitionToFailed_ShouldSucceed_WhenPaymentInProcessingState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.PROCESSING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.transitionToFailed(paymentId);

        // Then
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void transitionToFailed_ShouldThrowException_WhenPaymentNotInProcessingState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(InvalidPaymentStateException.class, () -> paymentService.transitionToFailed(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void transitionToCancelled_ShouldSucceed_WhenPaymentInCreatedState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.CREATED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.transitionToCancelled(paymentId);

        // Then
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void transitionToCancelled_ShouldSucceed_WhenPaymentInProcessingState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.PROCESSING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.transitionToCancelled(paymentId);

        // Then
        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void transitionToCancelled_ShouldThrowException_WhenPaymentInSuccessState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(InvalidPaymentStateException.class, () -> paymentService.transitionToCancelled(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void transitionToCancelled_ShouldThrowException_WhenPaymentInFailedState() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(InvalidPaymentStateException.class, () -> paymentService.transitionToCancelled(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void transitionToCancelled_ShouldThrowException_WhenPaymentAlreadyCancelled() {
        // Given
        UUID paymentId = payment.getId();
        payment.setStatus(PaymentStatus.CANCELLED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(InvalidPaymentStateException.class, () -> paymentService.transitionToCancelled(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
