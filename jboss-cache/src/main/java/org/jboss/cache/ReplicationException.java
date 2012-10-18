package org.jboss.cache;

/**
 * Thrown when a replication problem occurred
 */
public class ReplicationException extends CacheException {

	private static final long serialVersionUID = 33172388691879866L;

	public ReplicationException() {
		super();
	}

	public ReplicationException(Throwable cause) {
		super(cause);
	}

	public ReplicationException(String msg) {
		super(msg);
	}

	public ReplicationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
