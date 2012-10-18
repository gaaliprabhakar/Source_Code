package org.jboss.cache;

/**
 * Thrown when operations on {@link org.jboss.cache.Cache} or
 * {@link org.jboss.cache.Node} fail unexpectedly.
 * <p/>
 * Specific subclasses such as {@link org.jboss.cache.lock.TimeoutException},
 * {@link org.jboss.cache.config.ConfigurationException} and
 * {@link org.jboss.cache.lock.LockingException} have more specific uses.
 * 
 * @author <a href="mailto:bela@jboss.org">Bela Ban</a>
 * @author <a href="mailto:manik AT jboss DOT org">Manik Surtani</a>
 */
public class CacheException extends RuntimeException {

	private static final long serialVersionUID = -4386393072593859164L;

	public CacheException() {
		super();
	}

	public CacheException(Throwable cause) {
		super(cause);
	}

	public CacheException(String msg) {
		super(msg);
	}

	public CacheException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
