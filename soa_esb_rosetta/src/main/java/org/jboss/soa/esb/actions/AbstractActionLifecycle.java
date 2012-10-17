package org.jboss.soa.esb.actions;

/**
 * Abstract class for lifecycle methods
 *  
 */
public abstract class AbstractActionLifecycle implements ActionLifecycle {
	
    /**
     * Initialise the action instance.
     * <p/>
     * This method is called after the action instance has been instantiated so that
     * configuration options can be validated.
     * 
     * @throws ActionLifecycleException for errors during initialisation.
     */
	public void initialise() throws ActionLifecycleException {
	}

    /**
     * Destroy the action instance.
     * <p/>
     * This method is called prior to the release of the action instance.  All
     * resources associated with this action instance should be released as the
     * instance will no longer be used.
     */
	public void destroy() throws ActionLifecycleException {
	}
}