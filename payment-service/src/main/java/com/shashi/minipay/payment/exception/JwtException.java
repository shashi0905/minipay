package com.shashi.minipay.exception;

/**
 * JwtException - thrown when JWT token operations fail.
 * This includes: invalid signature, expired token, malformed token, etc.
 */
public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
