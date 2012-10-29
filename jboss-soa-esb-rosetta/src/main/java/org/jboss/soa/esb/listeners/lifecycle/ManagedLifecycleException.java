package org.jboss.soa.esb.listeners.lifecycle;

public class ManagedLifecycleException extends Exception {
    
    private static final long serialVersionUID = -2461030864678547990L;

	public ManagedLifecycleException() {
	}

	public ManagedLifecycleException(final String message) {
		super(message);
	}

	public ManagedLifecycleException(final Throwable cause) {
		super(cause);
	}

	public ManagedLifecycleException(String message, Throwable cause) {
		super(message, cause);
	}
}
