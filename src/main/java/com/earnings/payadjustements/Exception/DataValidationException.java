package com.earnings.payadjustements.Exception;
@SuppressWarnings("serial")
public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }
}
