package com.earnings.payadjustements.entity;

import java.util.List;

public class Response {
	private String status;
	private String message;
	private String code;
	private List<FailedEmployee> failedList; // Holds the failed entries

	public Response(String status, String message, String code, List<FailedEmployee> failedList) {
		this.status = status;
		this.message = message;
		this.code = code;
		this.failedList = failedList;
	}

	// Getters and Setters
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<FailedEmployee> getFailedList() {
		return failedList;
	}

	public void setFailedList(List<FailedEmployee> failedList) {
		this.failedList = failedList;
	}
}
