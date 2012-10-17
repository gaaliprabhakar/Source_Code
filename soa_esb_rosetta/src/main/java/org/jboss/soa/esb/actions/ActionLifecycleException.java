package org.jboss.soa.esb.actions;

/**
 * Exception representing errors in an action life cycle.
 */
public class ActionLifecycleException extends Exception {
	
    private static final long serialVersionUID = 3275504884247607474L;

	public ActionLifecycleException() {
	}

	public ActionLifecycleException(final String message) {
		super(message);
	}

	public ActionLifecycleException(final Throwable cause) {
		super(cause);
	}

	public ActionLifecycleException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
