package com.earnings.payadjustements.Exception;
@SuppressWarnings("serial")
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}