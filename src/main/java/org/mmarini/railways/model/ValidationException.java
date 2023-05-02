package org.mmarini.railways.model;

/**
 * @author $$Author: marco $$
 * @version $Id: ValidationException.java,v 1.3 2006/08/29 16:42:38 marco Exp $
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ValidationException() {
		super();
	}

	/**
	 * @param message
	 */
	public ValidationException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ValidationException(Throwable cause) {
		super(cause);
	}

}