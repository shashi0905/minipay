package com.shashi.minipay.exception;

/**
 * InvalidCredentialsException - thrown when login fails (user not found or password mismatch).
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
