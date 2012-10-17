package org.jboss.soa.esb.actions;

/**
 * Interface representing lifecycle methods of an action. 
 * <p/>
 * Actions implementing this interface will participate in the application
 * lifecycle and should not contain any state specific to a particular message
 * instance.  Each {@link org.jboss.soa.esb.listeners.message.ActionProcessingPipeline} will instantiate a single
 * instance of the action to process all messages passing through the pipeline.  
 *  
 */
public interface ActionLifecycle {
	
    /**
     * Initialise the action instance.
     * <p/>
     * This method is called after the action instance has been instantiated so that
     * configuration options can be validated.
     * 
     * @throws ActionLifecycleException for errors during initialisation.
     */
	public void initialise() throws ActionLifecycleException;

    /**
     * Destroy the action instance.
     * <p/>
     * This method is called prior to the release of the action instance.  All
     * resources associated with this action instance should be released as the
     * instance will no longer be used.
     */
	public void destroy() throws ActionLifecycleException;
}