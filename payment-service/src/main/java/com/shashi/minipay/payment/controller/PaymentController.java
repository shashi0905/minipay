package com.shashi.minipay.payment.controller;

import com.shashi.minipay.payment.dto.request.CreatePaymentRequest;
import com.shashi.minipay.payment.dto.response.CreatePaymentResponse;
import com.shashi.minipay.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create a payment with idempotency support.
     * The Idempotency-Key header ensures duplicate requests return the same payment.
     */
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        // Prefer authenticated userId from JWT (set by JwtAuthenticationFilter)
        Object userIdAttr = httpRequest.getAttribute("userId");
        String userId = userIdAttr != null ? userIdAttr.toString() : request.userId();

        CreatePaymentRequest requestWithIdempotencyKey = new CreatePaymentRequest(
                request.orderId(),
                userId,
                request.amount(),
                request.currency(),
                idempotencyKey
        );

        CreatePaymentResponse response = paymentService.createPayment(requestWithIdempotencyKey);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get payment by payment ID.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<CreatePaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        CreatePaymentResponse paymentResponse = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * Get payment by order ID.
     */
    @GetMapping
    public ResponseEntity<CreatePaymentResponse> getPaymentByOrderId(@RequestParam String orderId) {
        CreatePaymentResponse paymentResponse = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * Process payment with the external payment provider.
     * This endpoint triggers the actual payment processing through the mock provider.
     */
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<CreatePaymentResponse> processPayment(
            @PathVariable UUID paymentId,
            @RequestParam String paymentMethodId) {
        CreatePaymentResponse response = paymentService.processPayment(paymentId, paymentMethodId);
        return ResponseEntity.ok(response);
    }

}
