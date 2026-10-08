package com.shashi.minipay.provider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shashi.minipay.provider.dto.ChargeRequest;
import com.shashi.minipay.provider.dto.ChargeResponse;
import com.shashi.minipay.provider.dto.RefundRequest;
import com.shashi.minipay.provider.dto.RefundResponse;
import com.shashi.minipay.provider.service.PaymentProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentProviderController.class)
class PaymentProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentProviderService paymentProviderService;

    @BeforeEach
    void setUp() {
        reset(paymentProviderService);
    }

    @Test
    void createCharge_Success() throws Exception {
        ChargeRequest request = new ChargeRequest(
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        ChargeResponse response = new ChargeResponse(
                "ch_123",
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "succeeded",
                "pm_card_visa",
                Instant.now(),
                null,
                null
        );

        when(paymentProviderService.createCharge(any(ChargeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ch_123"))
                .andExpect(jsonPath("$.orderId").value("order_123"))
                .andExpect(jsonPath("$.status").value("succeeded"));

        verify(paymentProviderService, times(1)).createCharge(any(ChargeRequest.class));
    }

    @Test
    void getCharge_Success() throws Exception {
        ChargeResponse response = new ChargeResponse(
                "ch_123",
                "order_123",
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "succeeded",
                "pm_card_visa",
                Instant.now(),
                null,
                null
        );

        when(paymentProviderService.getCharge("ch_123")).thenReturn(response);

        mockMvc.perform(get("/v1/charges/ch_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ch_123"))
                .andExpect(jsonPath("$.status").value("succeeded"));

        verify(paymentProviderService, times(1)).getCharge("ch_123");
    }

    @Test
    void getCharge_NotFound() throws Exception {
        when(paymentProviderService.getCharge("non_existent"))
                .thenThrow(new IllegalArgumentException("Charge not found: non_existent"));

        mockMvc.perform(get("/v1/charges/non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Charge not found: non_existent"));

        verify(paymentProviderService, times(1)).getCharge("non_existent");
    }

    @Test
    void createRefund_Success() throws Exception {
        RefundRequest request = new RefundRequest(
                "ch_123",
                new BigDecimal("50.00"),
                "Customer requested refund"
        );

        RefundResponse response = new RefundResponse(
                "re_123",
                "ch_123",
                new BigDecimal("50.00"),
                "USD",
                "succeeded",
                Instant.now()
        );

        when(paymentProviderService.createRefund(any(RefundRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/charges/ch_123/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("re_123"))
                .andExpect(jsonPath("$.chargeId").value("ch_123"))
                .andExpect(jsonPath("$.status").value("succeeded"));

        verify(paymentProviderService, times(1)).createRefund(any(RefundRequest.class));
    }

    @Test
    void createRefund_ChargeNotFound() throws Exception {
        RefundRequest request = new RefundRequest(
                "non_existent",
                new BigDecimal("50.00"),
                "Customer requested refund"
        );

        when(paymentProviderService.createRefund(any(RefundRequest.class)))
                .thenThrow(new IllegalArgumentException("Charge not found: non_existent"));

        mockMvc.perform(post("/v1/charges/non_existent/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Charge not found: non_existent"));

        verify(paymentProviderService, times(1)).createRefund(any(RefundRequest.class));
    }

    @Test
    void getRefund_Success() throws Exception {
        RefundResponse response = new RefundResponse(
                "re_123",
                "ch_123",
                new BigDecimal("50.00"),
                "USD",
                "succeeded",
                Instant.now()
        );

        when(paymentProviderService.getRefund("re_123")).thenReturn(response);

        mockMvc.perform(get("/v1/charges/refunds/re_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("re_123"))
                .andExpect(jsonPath("$.chargeId").value("ch_123"))
                .andExpect(jsonPath("$.status").value("succeeded"));

        verify(paymentProviderService, times(1)).getRefund("re_123");
    }

    @Test
    void getRefund_NotFound() throws Exception {
        when(paymentProviderService.getRefund("non_existent"))
                .thenThrow(new IllegalArgumentException("Refund not found: non_existent"));

        mockMvc.perform(get("/v1/charges/refunds/non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Refund not found: non_existent"));

        verify(paymentProviderService, times(1)).getRefund("non_existent");
    }

    @Test
    void reset_Success() throws Exception {
        doNothing().when(paymentProviderService).reset();

        mockMvc.perform(post("/v1/charges/reset"))
                .andExpect(status().isOk())
                .andExpect(content().string("Provider state reset successfully"));

        verify(paymentProviderService, times(1)).reset();
    }

    @Test
    void createCharge_ValidationError() throws Exception {
        ChargeRequest request = new ChargeRequest(
                "",  // invalid: blank orderId
                "user_456",
                new BigDecimal("100.00"),
                "USD",
                "pm_card_visa",
                "idempotency_key_1"
        );

        mockMvc.perform(post("/v1/charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(paymentProviderService, never()).createCharge(any(ChargeRequest.class));
    }
}
