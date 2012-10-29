package org.jboss.soa.esb;


public class BaseException extends Exception {

	private static final long serialVersionUID = 1L;

	public BaseException() {
		super();
	}

	public BaseException(String message) {
		super(message);
	}

	/**
	 * Public Constructor.
	 * @param message Exception message.
	 * @param cause Exception cause.
	 */
	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(Throwable cause) {
		super(cause);
	}
}
