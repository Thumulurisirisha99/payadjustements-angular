package com.earnings.payadjustements.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TdsKeyEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "TRANSACTIONID")
	private int transactionId;

	@Column(name = "BUSINESSUNITID")
	private int businessUnitId;

	@Column(name = "EMPLOYEEID")
	private int employeeId;

	// Default constructor
	public TdsKeyEntity() {
	}

	// Parameterized constructor
	public TdsKeyEntity(int transactionId, int businessUnitId, int employeeId) {
		this.transactionId = transactionId;
		this.businessUnitId = businessUnitId;
		this.employeeId = employeeId;
	}

	// Getters and Setters
	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getBusinessUnitId() {
		return businessUnitId;
	}

	public void setBusinessUnitId(int businessUnitId) {
		this.businessUnitId = businessUnitId;
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	// equals() and hashCode() methods
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TdsKeyEntity that = (TdsKeyEntity) o;

		if (transactionId != that.transactionId)
			return false;
		if (businessUnitId != that.businessUnitId)
			return false;
		return employeeId == that.employeeId;
	}

	@Override
	public int hashCode() {
		int result = transactionId;
		result = 31 * result + businessUnitId;
		result = 31 * result + employeeId;
		return result;
	}
}
