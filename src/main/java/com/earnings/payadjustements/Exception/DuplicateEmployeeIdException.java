package com.earnings.payadjustements.Exception;
@SuppressWarnings("serial")
public class DuplicateEmployeeIdException extends RuntimeException {
    private final Integer employeeId;

    public DuplicateEmployeeIdException(String message, Integer employeeId) {
        super(message);
        this.employeeId = employeeId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }
}

