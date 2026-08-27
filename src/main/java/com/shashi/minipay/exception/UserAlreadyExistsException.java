package com.shashi.minipay.exception;

/**
 * UserAlreadyExistsException - thrown when attempting to register with duplicate username or email.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
