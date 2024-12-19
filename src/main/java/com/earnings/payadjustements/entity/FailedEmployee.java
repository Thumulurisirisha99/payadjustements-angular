package com.earnings.payadjustements.entity;

public class FailedEmployee {
    private Integer employeeId;
    private int rowIndex;

    public FailedEmployee(Integer employeeId, int rowIndex) {
        this.employeeId = employeeId;
        this.rowIndex = rowIndex;
    }

    public FailedEmployee(int i, int j, String errorMessage) {
		// TODO Auto-generated constructor stub
	}

	public Integer getEmployeeId() {
        return employeeId;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public String toString() {
        return "FailedEmployee{" +
                "employeeId=" + employeeId +
                ", rowIndex=" + rowIndex +
                '}';
    }
}
