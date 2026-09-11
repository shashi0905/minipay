package com.shashi.minipay.gateway.exception;

/**
 * JWT Exception for Gateway - thrown during token validation.
 */
public class GatewayJwtException extends RuntimeException {
    public GatewayJwtException(String message) {
        super(message);
    }

    public GatewayJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
