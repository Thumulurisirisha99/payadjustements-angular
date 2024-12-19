package com.earnings.payadjustements.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_employee_tds" , schema="hclhrm_prod")
public class Tds implements Serializable{

    @EmbeddedId
    private TdsKeyEntity id;  

    @Column(name = "TDSVALUE")
    private int TdsValue;

    @Column(name = "STATUS")
    private char status;

    @Column(name = "EMPLOYEESTATUS")
    private int employeeStatus;

    @Column(name = "CREATEDBY")
    private int createdBy;
    
    @Column(name = "datecreated", nullable = true)
    private Timestamp dateCreated;

    @Column(name = "MODIFIEDBY")
    private int modifiedBy;

    @Column(name = "DATEMODIFIED")
    private Timestamp dateModified;

    @Column(name = "VERIFIEDBY")
    private int verifiedBy;

    @Column(name = "DATEVERIFIED")
    private Timestamp dateVerified;

    @Column(name = "LOGID")
    private long logId;

    @Column(name = "LUDATE")
    private Timestamp luDate;

	/**
	 * @return the id
	 */
	public TdsKeyEntity getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(TdsKeyEntity id) {
		this.id = id;
	}


	/**
	 * @return the status
	 */
	public char getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(char status) {
		this.status = status;
	}

	/**
	 * @return the employeeStatus
	 */
	public int getEmployeeStatus() {
		return employeeStatus;
	}

	/**
	 * @param employeeStatus the employeeStatus to set
	 */
	public void setEmployeeStatus(int employeeStatus) {
		this.employeeStatus = employeeStatus;
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

	/**
	 * @return the dateCreated
	 */
	public Timestamp getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the modifiedBy
	 */
	public int getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(int modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * @return the dateModified
	 */
	public Timestamp getDateModified() {
		return dateModified;
	}

	/**
	 * @param dateModified the dateModified to set
	 */
	public void setDateModified(Timestamp dateModified) {
		this.dateModified = dateModified;
	}

	/**
	 * @return the verifiedBy
	 */
	public int getVerifiedBy() {
		return verifiedBy;
	}

	/**
	 * @param verifiedBy the verifiedBy to set
	 */
	public void setVerifiedBy(int verifiedBy) {
		this.verifiedBy = verifiedBy;
	}

	/**
	 * @return the dateVerified
	 */
	public Timestamp getDateVerified() {
		return dateVerified;
	}

	/**
	 * @param dateVerified the dateVerified to set
	 */
	public void setDateVerified(Timestamp dateVerified) {
		this.dateVerified = dateVerified;
	}

	/**
	 * @return the logId
	 */
	public long getLogId() {
		return logId;
	}

	/**
	 * @param logId the logId to set
	 */
	public void setLogId(long logId) {
		this.logId = logId;
	}

	/**
	 * @return the luDate
	 */
	public Timestamp getLuDate() {
		return luDate;
	}

	/**
	 * @param luDate the luDate to set
	 */
	public void setLuDate(Timestamp luDate) {
		this.luDate = luDate;
	}

	/**
	 * @return the tdsValue
	 */
	public int getTdsValue() {
		return TdsValue;
	}

	/**
	 * @param tdsValue the tdsValue to set
	 */
	public void setTdsValue(int tdsValue) {
		TdsValue = tdsValue;
	}

    // Getters and Setters
}
