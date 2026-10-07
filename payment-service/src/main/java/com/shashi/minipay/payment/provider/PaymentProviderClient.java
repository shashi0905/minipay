package com.shashi.minipay.payment.provider;

import com.shashi.minipay.payment.provider.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class PaymentProviderClient {

    private final WebClient webClient;

    public PaymentProviderClient(
            WebClient.Builder webClientBuilder,
            @Value("${payment.provider.base-url:http://localhost:8084}") String providerBaseUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(providerBaseUrl)
                .build();
    }

    public Mono<ProviderChargeResponse> createCharge(ProviderChargeRequest request) {
        return webClient.post()
                .uri("/v1/charges")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ProviderChargeResponse.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    try {
                        ProviderErrorResponse error = ex.getResponseBodyAs(ProviderErrorResponse.class);
                        if (error != null) {
                            return new PaymentProviderException(error.message(), error.type());
                        }
                    } catch (Exception ignored) {
                    }
                    return new PaymentProviderException("Provider error: " + ex.getMessage(), "api_error");
                });
    }

    public Mono<ProviderChargeResponse> getCharge(String chargeId) {
        return webClient.get()
                .uri("/v1/charges/{chargeId}", chargeId)
                .retrieve()
                .bodyToMono(ProviderChargeResponse.class)
                .onErrorMap(WebClientResponseException.NotFound.class, ex ->
                        new PaymentProviderException("Charge not found: " + chargeId, "invalid_request_error")
                )
                .onErrorMap(WebClientResponseException.class, ex ->
                        new PaymentProviderException("Provider error: " + ex.getMessage(), "api_error")
                );
    }

    public Mono<ProviderRefundResponse> createRefund(String chargeId, ProviderRefundRequest request) {
        return webClient.post()
                .uri("/v1/charges/{chargeId}/refunds", chargeId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ProviderRefundResponse.class)
                .onErrorMap(WebClientResponseException.class, ex -> {
                    try {
                        ProviderErrorResponse error = ex.getResponseBodyAs(ProviderErrorResponse.class);
                        if (error != null) {
                            return new PaymentProviderException(error.message(), error.type());
                        }
                    } catch (Exception ignored) {
                    }
                    return new PaymentProviderException("Provider error: " + ex.getMessage(), "api_error");
                });
    }

    public Mono<ProviderRefundResponse> getRefund(String refundId) {
        return webClient.get()
                .uri("/v1/charges/refunds/{refundId}", refundId)
                .retrieve()
                .bodyToMono(ProviderRefundResponse.class)
                .onErrorMap(WebClientResponseException.NotFound.class, ex ->
                        new PaymentProviderException("Refund not found: " + refundId, "invalid_request_error")
                )
                .onErrorMap(WebClientResponseException.class, ex ->
                        new PaymentProviderException("Provider error: " + ex.getMessage(), "api_error")
                );
    }
}
