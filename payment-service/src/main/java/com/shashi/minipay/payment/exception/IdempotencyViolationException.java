package com.shashi.minipay.payment.exception;

public class IdempotencyViolationException extends RuntimeException {
    public IdempotencyViolationException(String message) {
        super(message);
    }

    public IdempotencyViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
