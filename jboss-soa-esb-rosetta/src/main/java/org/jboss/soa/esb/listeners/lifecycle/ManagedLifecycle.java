package org.jboss.soa.esb.listeners.lifecycle;

import org.jboss.soa.esb.helpers.ConfigTree;

public interface ManagedLifecycle {

	/**
	 * Initialise the managed instance.
	 * <p/>
	 * This method is called after the managed instance has been instantiated so
	 * that configuration options can be validated.
	 * 
	 * @throws ManagedLifecycleException for errors during initialisation.
	 */
	public void initialise() throws ManagedLifecycleException;

	/**
	 * Start the managed instance.
	 * <p/>
	 * This method is called to inform the managed instance that it can
	 * initialise resources prior to enabling the service.
	 */
	public void start() throws ManagedLifecycleException;

	/**
	 * Stop the managed instance.
	 * <p/>
	 * This method is called to inform the managed instance that it must disable
	 * resources associated with the running service. The service may choose to
	 * disable the resources asynchronously provided that any subsequent call to
	 * {@link #start()} or {@link #destroy()} blocks until these resources have
	 * been disabled.
	 */
	public void stop() throws ManagedLifecycleException;

	/**
	 * Destroy the managed instance.
	 * <p/>
	 * This method is called prior to the release of the managed instance. All
	 * resources associated with this managed instance should be released as the
	 * instance will no longer be used.
	 */
	public void destroy() throws ManagedLifecycleException;

	/**
	 * Get the state of the managed instance.
	 * 
	 * @return The managed instance state.
	 */
	public ManagedLifecycleState getState();

	/**
	 * Wait until the managed instance has transitioned into the DESTROYED
	 * state.
	 * 
	 * @return true if the transition occurs within the expected period, false
	 *         otherwise.
	 */
	public boolean waitUntilDestroyed();

	/**
	 * Wait until the managed instance has transitioned into the DESTROYED
	 * state.
	 * 
	 * @param transitionPeriod
	 *            The maximum delay expected for the transition, specified in
	 *            milliseconds.
	 * @return true if the transition occurs within the expected period, false
	 *         otherwise.
	 */
	public boolean waitUntilDestroyed(final long transitionPeriod);

	/**
	 * Add a managed lifecycle event listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void addManagedLifecycleEventListener(final ManagedLifecycleEventListener listener);

	/**
	 * Remove a managed lifecycle event listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	public void removeManagedLifecycleEventListener(final ManagedLifecycleEventListener listener);

	/**
	 * Get the configuration assoicated with the ManagedLifecycle.
	 * 
	 * @return Configuration.
	 */
	public ConfigTree getConfig();
}