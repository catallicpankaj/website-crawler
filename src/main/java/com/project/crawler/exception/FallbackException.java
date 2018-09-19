package com.project.crawler.exception;

import java.util.ArrayList;
import java.util.List;

import com.project.crawler.dto.ErrorMessage;

public class FallbackException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3176323285076013687L;
	
	/** The error messages. */
	private final List<ErrorMessage> errorMessages;

	/**
	 * Instantiates a new invalid input exception.
	 */
	public FallbackException() {
		this.errorMessages = new ArrayList<>();
	}

	/**
	 * Instantiates a new invalid input exception.
	 *
	 * @param errorMessages the error messages
	 */
	public FallbackException(List<ErrorMessage> errorMessages) {
		this();
		this.errorMessages.addAll(errorMessages);
	}
	
	/**
	 * Instantiates a new Invalid Input Exception.
	 * 
	 * @param errorMessage the error Messages
	 */
	public FallbackException(ErrorMessage errorMessage) {
		this();
		this.errorMessages.add(errorMessage);
		
	}

	/**
	 * Instantiates a new invalid input exception.
	 *
	 * @param errorMessages the error messages
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public FallbackException(List<ErrorMessage> errorMessages, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		this.errorMessages = new ArrayList<>();
		this.errorMessages.addAll(errorMessages);
	}

	/**
	 * Instantiates a new invalid input exception.
	 *
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public FallbackException(Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this.errorMessages = new ArrayList<>();
	}

	/**
	 * Instantiates a new invalid input exception.
	 *
	 * @param errorMessages the error messages
	 * @param cause the cause
	 */
	public FallbackException(List<ErrorMessage> errorMessages, Throwable cause) {
		this.errorMessages = new ArrayList<>();
		this.errorMessages.addAll(errorMessages);
	}

	/**
	 * Instantiates a new invalid input exception.
	 *
	 * @param cause the cause
	 */
	public FallbackException(Throwable cause) {
		this.errorMessages = new ArrayList<>();
	}

	/**
	 * Gets the error messages.
	 *
	 * @return the error messages
	 */
	public List<ErrorMessage> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * Sets the error messages.
	 *
	 * @param errorMessages the new error messages
	 */
	public void setErrorMessages(List<ErrorMessage> errorMessages) {
		this.errorMessages.addAll(errorMessages);
	}


}
