package org.jboss.cache.lock;

import org.jboss.cache.CacheException;

/**
 * Thrown when a timeout occurred. used by operations with timeouts, e.g. lock
 * acquisition, or waiting for responses from all members.
 * 
 * @author <a href="mailto:bela@jboss.org">Bela Ban</a>.
 * @version $Revision: 7168 $
 *          <p/>
 *          <p>
 *          <b>Revisions:</b>
 *          <p/>
 *          <p>
 *          Dec 28 2002 Bela Ban: first implementation
 */
public class TimeoutException extends CacheException {

	/**
	 * The serialVersionUID
	 */
	private static final long serialVersionUID = -8096787619908687038L;

	public TimeoutException() {
		super();
	}

	public TimeoutException(String msg) {
		super(msg);
	}

	public TimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
