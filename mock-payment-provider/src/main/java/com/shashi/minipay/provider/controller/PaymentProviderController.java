package com.shashi.minipay.provider.controller;

import com.shashi.minipay.provider.dto.ChargeRequest;
import com.shashi.minipay.provider.dto.ChargeResponse;
import com.shashi.minipay.provider.dto.ErrorResponse;
import com.shashi.minipay.provider.dto.RefundRequest;
import com.shashi.minipay.provider.dto.RefundResponse;
import com.shashi.minipay.provider.service.PaymentProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/charges")
@Tag(name = "Payment Provider API", description = "Mock payment provider API mimicking Stripe-like payment flows")
public class PaymentProviderController {

    private final PaymentProviderService paymentProviderService;

    public PaymentProviderController(PaymentProviderService paymentProviderService) {
        this.paymentProviderService = paymentProviderService;
    }

    @PostMapping
    @Operation(summary = "Create a charge", description = "Process a payment charge with the given payment method")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Charge created successfully", content = @Content(schema = @Schema(implementation = ChargeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ChargeResponse> createCharge(@Valid @RequestBody ChargeRequest request) {
        try {
            ChargeResponse response = paymentProviderService.createCharge(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                    "charge_error",
                    e.getMessage(),
                    "api_error"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{chargeId}")
    @Operation(summary = "Retrieve a charge", description = "Get details of a specific charge by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Charge retrieved successfully", content = @Content(schema = @Schema(implementation = ChargeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Charge not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getCharge(@PathVariable String chargeId) {
        try {
            ChargeResponse response = paymentProviderService.getCharge(chargeId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                    "charge_not_found",
                    e.getMessage(),
                    "invalid_request_error"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{chargeId}/refunds")
    @Operation(summary = "Create a refund", description = "Refund a previously successful charge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund created successfully", content = @Content(schema = @Schema(implementation = RefundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Charge not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createRefund(@PathVariable String chargeId, @Valid @RequestBody RefundRequest request) {
        try {
            RefundRequest refundRequest = new RefundRequest(chargeId, request.amount(), request.reason());
            RefundResponse response = paymentProviderService.createRefund(refundRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                    "refund_error",
                    e.getMessage(),
                    "invalid_request_error"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                    "refund_error",
                    e.getMessage(),
                    "api_error"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/refunds/{refundId}")
    @Operation(summary = "Retrieve a refund", description = "Get details of a specific refund by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund retrieved successfully", content = @Content(schema = @Schema(implementation = RefundResponse.class))),
            @ApiResponse(responseCode = "404", description = "Refund not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getRefund(@PathVariable String refundId) {
        try {
            RefundResponse response = paymentProviderService.getRefund(refundId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                    "refund_not_found",
                    e.getMessage(),
                    "invalid_request_error"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset provider state", description = "Clear all charges and refunds (for testing purposes)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider state reset successfully")
    })
    public ResponseEntity<String> reset() {
        paymentProviderService.reset();
        return ResponseEntity.ok("Provider state reset successfully");
    }
}
