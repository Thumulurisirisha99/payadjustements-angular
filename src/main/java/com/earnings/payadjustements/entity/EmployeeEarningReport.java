package com.earnings.payadjustements.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_employee_earningreport", schema = "hclhrm_prod")
public class EmployeeEarningReport {

	@EmbeddedId
	private IdEntity id;
	private String filename;
	private String filefailedemployees;
	private String reason;
	private int uploadedby;
	private Integer status;
	@Column(name = "createddate", nullable = false)
	private LocalDateTime createddate;

	@PrePersist
	protected void onCreate() {
		this.createddate = LocalDateTime.now(); 
	}

	/**
	 * @return the id
	 */
	public IdEntity getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(IdEntity id) {
		this.id = id;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the filepath
	 */

	/**
	 * @return the filestatus
	 */

	/**
	 * @return the filefailedemployees
	 */
	public String getFilefailedemployees() {
		return filefailedemployees;
	}

	/**
	 * @param filefailedemployees the filefailedemployees to set
	 */
	public void setFilefailedemployees(String filefailedemployees) {
		this.filefailedemployees = filefailedemployees;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the uploadedBy
	 */
	public int getUploadedby() {
		return uploadedby;
	}

	/**
	 * @param uploadedBy the uploadedBy to set
	 */
	public void setUploadedby(int uploadedby) {
		this.uploadedby = uploadedby;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the createddate
	 */
	public LocalDateTime getCreateddate() {
		return createddate;
	}

	/**
	 * @param createddate the createddate to set
	 */
	public void setCreateddate(LocalDateTime createddate) {
		this.createddate = createddate;
	}

	// Getters and Setters
}
