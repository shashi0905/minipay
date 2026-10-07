package com.shashi.minipay.payment.provider;

public class PaymentProviderException extends RuntimeException {

    private final String errorType;

    public PaymentProviderException(String message, String errorType) {
        super(message);
        this.errorType = errorType;
    }

    public String getErrorType() {
        return errorType;
    }
}
