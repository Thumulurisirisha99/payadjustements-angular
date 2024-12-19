package com.earnings.payadjustements.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class KeyEntity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="TRANSACTIONID")
	private int transactionId;
	@Column(name="BUSINESSUNITID")
    private int businessUnitId;
	@Column(name="EMPLOYEEID")
    private int employeeId;
	@Column(name="COMPONENTID")
    private int componentId;

    // Default constructor
    public KeyEntity() {}

    // Constructor, Getters, Setters, equals(), and hashCode()

    public KeyEntity(int transactionId, int businessUnitId, int employeeId, int componentId) {
        this.transactionId = transactionId;
        this.businessUnitId = businessUnitId;
        this.employeeId = employeeId;
        this.componentId = componentId;
    }

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

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyEntity keyEntity = (KeyEntity) o;

        if (transactionId != keyEntity.transactionId) return false;
        if (businessUnitId != keyEntity.businessUnitId) return false;
        if (employeeId != keyEntity.employeeId) return false;
        return componentId == keyEntity.componentId;
    }

    @Override
    public int hashCode() {
        int result = transactionId;
        result = 31 * result + businessUnitId;
        result = 31 * result + employeeId;
        result = 31 * result + componentId;
        return result;
    }
}
