package com.shashi.minipay.order.client;

import com.shashi.minipay.order.client.dto.CreatePaymentRequest;
import com.shashi.minipay.order.client.dto.CreatePaymentResponse;
import com.shashi.minipay.order.exception.PaymentServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${payment.service.base-url:http://localhost:8082}") String paymentServiceBaseUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(paymentServiceBaseUrl)
                .build();
    }

    public Mono<CreatePaymentResponse> createPayment(CreatePaymentRequest request, String idempotencyKey) {
        return webClient.post()
                .uri("/api/payments")
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreatePaymentResponse.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    String errorMessage = "Payment service error: " + ex.getMessage();
                    if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()) {
                        errorMessage = "Payment service error: " + ex.getResponseBodyAsString();
                    }
                    return new PaymentServiceException(errorMessage, ex);
                })
                .onErrorMap(Exception.class, ex -> {
                    return new PaymentServiceException("Failed to create payment: " + ex.getMessage(), ex);
                });
    }

    public Mono<CreatePaymentResponse> getPaymentById(String paymentId) {
        return webClient.get()
                .uri("/api/payments/{paymentId}", paymentId)
                .retrieve()
                .bodyToMono(CreatePaymentResponse.class)
                .onErrorMap(WebClientResponseException.NotFound.class, ex ->
                        new PaymentServiceException("Payment not found: " + paymentId)
                )
                .onErrorMap(WebClientResponseException.class, ex ->
                        new PaymentServiceException("Payment service error: " + ex.getMessage(), ex)
                );
    }

    public Mono<CreatePaymentResponse> processPayment(String paymentId, String paymentMethodId) {
        return webClient.post()
                .uri("/api/payments/{paymentId}/process?paymentMethodId={paymentMethodId}", paymentId, paymentMethodId)
                .retrieve()
                .bodyToMono(CreatePaymentResponse.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    String errorMessage = "Payment service error: " + ex.getMessage();
                    if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()) {
                        errorMessage = "Payment service error: " + ex.getResponseBodyAsString();
                    }
                    return new PaymentServiceException(errorMessage, ex);
                })
                .onErrorMap(Exception.class, ex -> {
                    return new PaymentServiceException("Failed to process payment: " + ex.getMessage(), ex);
                });
    }
}
