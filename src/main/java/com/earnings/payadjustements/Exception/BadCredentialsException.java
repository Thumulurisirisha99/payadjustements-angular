package com.earnings.payadjustements.Exception;
@SuppressWarnings("serial")
public class BadCredentialsException extends RuntimeException {
	public BadCredentialsException(String message) {
		super(message);
	}
}