package com.earnings.payadjustements.Exception;

public class PayPeriodMissMatchException extends RuntimeException {
    public PayPeriodMissMatchException(String message) {
        super(message);
    }
}