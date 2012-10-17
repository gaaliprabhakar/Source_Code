package org.jboss.soa.esb.listeners.lifecycle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.helpers.ConfigTree;

/**
 * This class represents the lifecycle for a managed instance.
 */
public abstract class AbstractManagedLifecycle implements ManagedLifecycle, Serializable {

    private static final Logger logger = Logger.getLogger(AbstractManagedLifecycle.class) ;
    
    /**
     * The name of the attribute specifying the termination period.
     */
    public static final String PARAM_TERMINATION_PERIOD = "terminationPeriod" ;
    
    /**
     * The lock used for state operations.
     */
    private final Lock stateLock = new ReentrantLock() ;
    /**
     * The condition used for state changes.
     */
    private final Condition stateChanged = stateLock.newCondition() ;
    
    /**
     * The state of the managed instance.
     */
    private transient ManagedLifecycleState state = ManagedLifecycleState.CONSTRUCTED ;
    /**
     * The maximum amount of time to wait for termination.
     */
    private long terminationPeriod = 60000 ;
    /**
     * The list of listeners associated with this managed instance.
     */
    private Set<ManagedLifecycleEventListener> listeners = new CopyOnWriteArraySet<ManagedLifecycleEventListener>() ;
    /**
     * Instance configuration.  Supplied through constructor.
     */
    private final ConfigTree config;

    /**
     * Lifecycle controller for this lifecycle.
     */
    private LifecycleController lifecycleController;
    
    /**
     * Construct the managed lifecycle.
     * @param config The configuration associated with this instance.
     * @throws ConfigurationException for configuration errors during initialisation.
     */
    protected AbstractManagedLifecycle(final ConfigTree config) throws ConfigurationException {
    	
        final String terminationPeriodVal = config.getAttribute(PARAM_TERMINATION_PERIOD) ;
		if (terminationPeriodVal != null) {
			try {
                this.terminationPeriod = (Long.parseLong(terminationPeriodVal) * 1000) ;
			} catch (final NumberFormatException nfe) {
                throw new ConfigurationException("Failed to parse " +
                    PARAM_TERMINATION_PERIOD + " value of " + terminationPeriodVal) ;
            }
		}

		if (logger.isDebugEnabled()) {
            logger.debug(PARAM_TERMINATION_PERIOD + " value " + terminationPeriod) ;
        }

        this.config = config;
       
        lifecycleController = new LifecycleController(new LifecycleControllerAdapter());
    }
    
    /**
     * Initialise the managed instance.
     * <p/>
     * This method is called after the managed instance has been instantiated so that
     * configuration options can be validated.
     * 
     * @throws ManagedLifecycleException for errors during initialisation.
     */
	public final void initialise() throws ManagedLifecycleException {
		if (!ManagedLifecycleState.INITIALISED.equals(getState())) {
			changeState(ManagedLifecycleState.INITIALISING);
			try {
                doInitialise() ;
                changeState(ManagedLifecycleState.INITIALISED) ;
                lifecycleController.registerMBean();
			} catch (final ManagedLifecycleException mle) {
				changeState(ManagedLifecycleState.DESTROYED);
				throw mle;
			} catch (final Exception ex) {
				logger.warn("Unexpected exception caught while initialisation",
						ex);
				changeState(ManagedLifecycleState.DESTROYED);
				throw new ManagedLifecycleException(ex);
			}
		}
	}

    /**
     * Handle the initialisation of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while initialisation.
     */
    protected abstract void doInitialise()
    	throws ManagedLifecycleException ;

    /**
     * Start the managed instance.
     * <p/>
     * This method is called to inform the managed instance that it can initialise
     * resources prior to enabling the service.
     */
	public final void start() throws ManagedLifecycleException {
		if (!ManagedLifecycleState.STARTED.equals(getState())) {
			changeState(ManagedLifecycleState.STARTING);
			try {
                doStart() ;
                changeState(ManagedLifecycleState.STARTED) ;
                lifecycleController.setStartTime(System.currentTimeMillis());
			} catch (final ManagedLifecycleException mle) {
				changeState(ManagedLifecycleState.STOPPED);
				throw mle;
			} catch (final Exception ex) {
                logger.warn("Unexpected exception caught while starting", ex) ;
                changeState(ManagedLifecycleState.STOPPED) ;
                throw new ManagedLifecycleException(ex) ;
            }
        }
    }
    
    /**
     * Handle the start of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while starting.
     */
	protected abstract void doStart() throws ManagedLifecycleException;

    /**
     * Stop the managed instance.
     * <p/>
     * This method is called to inform the managed instance that it must disable
     * resources associated with the running service.  The service may choose to
     * disable the resources asynchronously provided that any subsequent call to
     * {@link #start()} or {@link #destroy()} blocks until these resources have been
     * disabled. 
     */
	public final void stop() throws ManagedLifecycleException {
		
		if (!ManagedLifecycleState.STOPPED.equals(getState())) {
			changeState(ManagedLifecycleState.STOPPING);
			try {
				doStop();
			} catch (final ManagedLifecycleException mle) {
				throw mle;
			} catch (final Exception ex) {
				logger.warn("Unexpected exception caught while stopping", ex);
				throw new ManagedLifecycleException(ex);
			} finally {
				changeState(ManagedLifecycleState.STOPPED);
				lifecycleController.unsetStartTime();
			}
		}
	}

    /**
     * Handle the stop of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while stopping.
     */
    protected abstract void doStop() throws ManagedLifecycleException ;

    /**
     * Destroy the managed instance.
     * <p/>
     * This method is called prior to the release of the managed instance.  All
     * resources associated with this managed instance should be released as the
     * instance will no longer be used.
     */
	public final void destroy() throws ManagedLifecycleException {
		
		if (!ManagedLifecycleState.DESTROYED.equals(getState())) {
			changeState(ManagedLifecycleState.DESTROYING);
			lifecycleController.unregisterMBean();
			try {
				doDestroy();
			} catch (final ManagedLifecycleException mle) {
				throw mle;
			} catch (final Exception ex) {
				logger.warn("Unexpected exception caught while destroying", ex);
				throw new ManagedLifecycleException(ex);
			} finally {
                changeState(ManagedLifecycleState.DESTROYED) ;
            }
        }
    }

    /**
     * Handle the destroy of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while destroying.
     */
    protected abstract void doDestroy() throws ManagedLifecycleException ;
    
    /**
     * Get the state of the managed instance.
     * @return The managed instance state.
     */
	public ManagedLifecycleState getState() {
		stateLock.lock();
		try {
			return state;
		} finally {
			stateLock.unlock();
		}
	}
    
    /**
     * Change the state of the managed instance.
     * @param newState The new state of the managed instance.
     * @throws ManagedLifecycleException 
     */
	protected void changeState(final ManagedLifecycleState newState) throws ManagedLifecycleException {
        final ManagedLifecycleState origState ;
        stateLock.lock() ;
		try {
			if (!state.canTransition(newState)) {
                throw new ManagedLifecycleException("Invalid state change from " + state + " to " + newState) ;
			}
			origState = state;
			state = newState;
			stateChanged.signalAll();
		} finally {
			stateLock.unlock() ;
        }
        fireStateChangedEvent(origState, newState) ;
    }
    
    /**
     * Get the termination period for this service.
     * @return The termination period.
     */
	protected long getTerminationPeriod() {
		return terminationPeriod;
	}
    
    /**
     * Wait until the managed instance has transitioned into the DESTROYED state.
     * @return true if the transition occurs within the expected period, false otherwise.
     */
	public boolean waitUntilDestroyed() {
        return waitUntilDestroyed(getTerminationPeriod()) ;
    }
    
    /**
     * Wait until the managed instance has transitioned into the DESTROYED state.
     * @param transitionPeriod The maximum delay expected for the transition, specified in milliseconds.
     * @return true if the transition occurs within the expected period, false otherwise.
     */
	public boolean waitUntilDestroyed(final long transitionPeriod) {
        return waitUntilState(ManagedLifecycleState.DESTROYED, transitionPeriod) ;
    }
    
    
    /**
     * Wait until the managed instance has transitioned into the specified state.
     * @param state The expected state.
     * @param transitionPeriod The maximum delay expected for the transition, specified in milliseconds.
     * @return true if the transition occurs within the expected period, false otherwise.
     */
	protected boolean waitUntilState(final ManagedLifecycleState state, final long transitionPeriod) {
        return waitForStateChange(state, transitionPeriod, true) ;
    }
    
    /**
     * Wait until the managed instance is not in the specified state.
     * @param state The original state.
     * @param transitionPeriod The maximum delay expected for the transition, specified in milliseconds.
     * @return true if the transition occurs within the expected period, false otherwise.
     */
    protected boolean waitUntilNotState(final ManagedLifecycleState state, final long transitionPeriod) {
        return waitForStateChange(state, transitionPeriod, false) ;
    }
    
    
    /**
     * Wait until the managed instance has transitioned.
     * @param state The specified state.
     * @param transitionPeriod The maximum delay expected for the transition, specified in milliseconds.
     * @param equality True if the state should be equal to the specified state, false otherwise.
     * @return true if the transition occurs within the expected period, false otherwise.
     */
    private boolean waitForStateChange(final ManagedLifecycleState state, final long transitionPeriod, final boolean equality) {
		try {
			stateLock.lock();
			try {
				if (equality ^ (this.state == state)) {
                    final long end = System.currentTimeMillis() + transitionPeriod ;
					while (equality ^ (this.state == state)) {
						final long delay = end - System.currentTimeMillis();
						if (delay <= 0) {
							break;
						}
                        stateChanged.await(delay, TimeUnit.MILLISECONDS) ;
					}
				}
				return !(equality ^ (this.state == state));
			} finally {
				stateLock.unlock();
			}
		} catch (final InterruptedException ie) {
			if (logger.isInfoEnabled()) {
				logger.info("Interrupted while waiting for state change");
			}
            
			stateLock.lock();
			try {
				return (this.state == state);
			} finally {
				stateLock.unlock();
			}
        }
    }
    
    /**
     * Add a managed lifecycle event listener.
     * @param listener The listener.
     */
	public void addManagedLifecycleEventListener(final ManagedLifecycleEventListener listener) {
        listeners.add(listener) ;
    }
    
    /**
     * Remove a managed lifecycle event listener.
     * @param listener The listener.
     */
	public void removeManagedLifecycleEventListener(final ManagedLifecycleEventListener listener) {
        listeners.remove(listener) ;
    }
    
    /**
     * Fire the state changed event.
     * @param origState The original state, prior to transition
     * @param newState The new state after transition
     */
    private void fireStateChangedEvent(final ManagedLifecycleState origState, final ManagedLifecycleState newState) {
		if (listeners.size() > 0) {
            final ManagedLifecycleStateEvent event = new ManagedLifecycleStateEvent(this, origState, newState) ;
            for(ManagedLifecycleEventListener listener: listeners){
                listener.stateChanged(event) ;
            }
        }
    }
    
    /**
     * Deserialise this managed lifecycle.
     * @param in The input stream.
     * @throws IOException for errors generated by the input stream.
     * @throws ClassNotFoundException For classpath errors.
     */
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject() ;
        state = ManagedLifecycleState.CONSTRUCTED ;
    }

    /**
     * Get the configuration associated with this lifecycle.
     * @return The instance configuration.
     */
	public ConfigTree getConfig() {
        return config;
    }
    
    private final class LifecycleControllerAdapter implements ManagedLifecycleAdapter {
        /**
         * Start the managed instance.
         * <p/>
         * This method is called to inform the managed instance that it can initialise
         * resources prior to enabling the service.
         */
		public void start() throws ManagedLifecycleException {
			AbstractManagedLifecycle.this.start();
		}

        /**
         * Stop the managed instance.
         * <p/>
         * This method is called to inform the managed instance that it must disable
         * resources associated with the running service.  The service may choose to
         * disable the resources asynchronously provided that any subsequent call to
         * {@link #start()} or {@link #destroy()} blocks until these resources have been
         * disabled. 
         */
		public void stop() throws ManagedLifecycleException {
			AbstractManagedLifecycle.this.stop();
		}

        /**
         * Get the state of the managed instance.
         * @return The managed instance state.
         */
		public ManagedLifecycleState getState() {
			return AbstractManagedLifecycle.this.getState();
		}

        /**
         * Get the configuration assoicated with the ManagedLifecycle.
         * @return Configuration.
         */
		public ConfigTree getConfig() {
			return AbstractManagedLifecycle.this.getConfig();
		}
    }
}
