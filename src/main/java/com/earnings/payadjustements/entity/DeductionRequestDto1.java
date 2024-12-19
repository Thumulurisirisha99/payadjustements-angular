package com.earnings.payadjustements.entity;

import java.util.List;

public class DeductionRequestDto1 {
	 private int createdBy;
	 private Integer employeeId;
	    private Integer transactionId;
	    private Integer businessUnitId;
	    private List<Component> components;
		/**
		 * @return the employeeId
		 */
		public Integer getEmployeeId() {
			return employeeId;
		}
		/**
		 * @param employeeId the employeeId to set
		 */
		public void setEmployeeId(Integer employeeId) {
			this.employeeId = employeeId;
		}
		/**
		 * @return the transactionId
		 */
		public Integer getTransactionId() {
			return transactionId;
		}
		/**
		 * @param transactionId the transactionId to set
		 */
		public void setTransactionId(Integer transactionId) {
			this.transactionId = transactionId;
		}
		/**
		 * @return the businessUnitId
		 */
		public Integer getBusinessUnitId() {
			return businessUnitId;
		}
		/**
		 * @param businessUnitId the businessUnitId to set
		 */
		public void setBusinessUnitId(Integer businessUnitId) {
			this.businessUnitId = businessUnitId;
		}
		/**
		 * @return the components
		 */
		public List<Component> getComponents() {
			return components;
		}
		/**
		 * @param components the components to set
		 */
		public void setComponents(List<Component> components) {
			this.components = components;
		}
		/**
		 * @return the createdBy
		 */
		public int getCreatedBy() {
			return createdBy;
		}
		/**
		 * @param createdBy the createdBy to set
		 */
		public void setCreatedBy(int createdBy) {
			this.createdBy = createdBy;
		}
		
}
