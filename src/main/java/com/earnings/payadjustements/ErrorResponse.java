package com.earnings.payadjustements;

public class ErrorResponse {
	private String error;
	private String message;
	 private String status;

	public ErrorResponse(String error, String message,String status) {
		this.error = error;
		this.message = message;
		this.status=status;
	}

	public ErrorResponse(String string, String string2) {
		// TODO Auto-generated constructor stub
	}

	 public ErrorResponse(boolean success, String message, int statusCode) {
	        this.error = success ? "false" : "true";
	        this.message = message;
	        this.status = String.valueOf(statusCode);
	    }

	public ErrorResponse(String string, int i) {
		// TODO Auto-generated constructor stub
	}

	public ErrorResponse(String error2, String message2, int i) {
		// TODO Auto-generated constructor stub
	}

	public ErrorResponse() {
		// TODO Auto-generated constructor stub
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}