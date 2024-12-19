package com.earnings.payadjustements.customException;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.earnings.payadjustements.ErrorResponse;
import com.earnings.payadjustements.Exception.BadCredentialsException;
import com.earnings.payadjustements.Exception.DataValidationException;
import com.earnings.payadjustements.Exception.DuplicateEmployeeIdException;
import com.earnings.payadjustements.Exception.EmployeeNotFoundException;
import com.earnings.payadjustements.Exception.MissingHeaderException;
import com.earnings.payadjustements.Exception.NoDataFoundException;
import com.earnings.payadjustements.Exception.PayPeriodMissMatchException;
import com.earnings.payadjustements.entity.Response;

@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(NoDataFoundException.class)
	public ResponseEntity<String> handleNoDataFoundException(NoDataFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<String> handleNoDataFoundException(BadCredentialsException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(DataValidationException.class)
	public ResponseEntity<String> handleValidation(DataValidationException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("true");
        errorResponse.setMessage(ex.getMessage()); // You can customize the message here.
        errorResponse.setStatus(String.valueOf(HttpStatus.UNAUTHORIZED.value())); // "401"
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//        ErrorResponse errorResponse = new ErrorResponse();
//        errorResponse.setError("true");
//        errorResponse.setMessage("Unexpected error occurred.");
//        errorResponse.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())); // "500"
//        
//        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<Response> handleMissingHeaderException(MissingHeaderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response("false", ex.getMessage(), String.valueOf(HttpStatus.BAD_REQUEST.value()), Collections.emptyList()));
    }
    @ExceptionHandler(DuplicateEmployeeIdException.class)
    public ResponseEntity<Response> handleDuplicateEmployeeIdException(DuplicateEmployeeIdException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response("false", ex.getMessage(), String.valueOf(HttpStatus.BAD_REQUEST.value()), Collections.emptyList()));
    }
    @ExceptionHandler(PayPeriodMissMatchException.class)
    public ResponseEntity<Response> handlePayPeriodMissMatchException(PayPeriodMissMatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response("false", ex.getMessage(), String.valueOf(HttpStatus.BAD_REQUEST.value()), Collections.emptyList()));
    }
    
}
