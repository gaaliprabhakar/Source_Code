package org.jboss.soa.esb.listeners.lifecycle;

import java.io.IOException;
import java.io.ObjectInputStream;
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
 * This class provides threaded support for a managed instance.
 */
public abstract class AbstractThreadedManagedLifecycle extends AbstractManagedLifecycle implements Runnable {
   
	private static final long serialVersionUID = -3416897438000324214L;

    private static final Logger logger = Logger.getLogger(AbstractThreadedManagedLifecycle.class) ;
    
    /**
     * The lock used for managing the running state.
     */
    private final Lock runningLock = new ReentrantLock() ;
    
    /**
     * The condition used for running state changes.
     */
    private final Condition runningChanged = runningLock.newCondition() ;
    
    /**
     * The running state.
     */
    private transient ManagedLifecycleThreadState state = ManagedLifecycleThreadState.STOPPED ;
    
    /**
     * The list of listeners associated with this managed instance.
     */
    private Set<ManagedLifecycleThreadEventListener> listeners = new CopyOnWriteArraySet<ManagedLifecycleThreadEventListener>() ;
    

    /**
     * Construct the threaded managed lifecycle.
     * @param config The configuration associated with this instance.
     * @throws ConfigurationException for configuration errors during initialisation.
     */
    protected AbstractThreadedManagedLifecycle(final ConfigTree config)throws ConfigurationException{
        super(config) ;
    }
    
    /**
     * Handle the start of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while starting.
     */
    protected void doStart() throws ManagedLifecycleException {
    	
        runningLock.lock() ;
		try {
			if (!waitUntilStopped()) {
                throw new ManagedLifecycleException("Thread still active from previous start") ;
            }
            setRunning(ManagedLifecycleThreadState.RUNNING) ;
		} finally {
            runningLock.unlock() ;
        }
        final Thread thread = new Thread(this) ;
        thread.start() ;
    }
    
    /**
     * The thread execution method.
     */
	public final void run() {
        waitUntilNotState(ManagedLifecycleState.STARTING, getTerminationPeriod()) ;
		try {
            changeState(ManagedLifecycleState.RUNNING) ;
            doRun() ;
		} catch (final ManagedLifecycleException mle) {
            // State change was not allowed, we are already stopping.
		} catch (final Throwable th) {
            logger.warn("Unexpected error from doRun()", th) ;
		} finally {
            setRunning(ManagedLifecycleThreadState.STOPPED) ;
        }
    }
    
    /**
     * Execute on the thread.
     */
    protected abstract void doRun() ;
    
    /**
     * Handle the stop of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while stopping.
     */
	protected void doStop() throws ManagedLifecycleException {
		
        runningLock.lock() ;
		try {
			if (isRunning()) {
				setRunning(ManagedLifecycleThreadState.STOPPING);
			}
		} finally {
            runningLock.unlock() ;
        }
    }

    /**
     * Handle the destroy of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while destroying.
     */
    protected final void doDestroy() throws ManagedLifecycleException {
    	
		if (!waitUntilStopped()) {
            throw new ManagedLifecycleException("Thread still active") ;
        }
        
        doThreadedDestroy() ;
    }
    
    /**
     * Handle the threaded destroy of the managed instance.
     * 
     * @throws ManagedLifecycleException for errors while destroying.
     */
    protected void doThreadedDestroy() throws ManagedLifecycleException {
    }
    
    /**
     * Is the associated thread still running?
     * @return true if the thread is still running, false otherwise.
     */
	public boolean isRunning() {
        return checkState(ManagedLifecycleThreadState.RUNNING) ;
    }
    
    /**
     * Is the associated thread stopped?
     * @return true if the thread is stopped, false otherwise.
     */
	public boolean isStopped() {
        return checkState(ManagedLifecycleThreadState.STOPPED) ;
    }
    
    /**
     * Is the associated thread stopping?
     * @return true if the thread is stopped, false otherwise.
     */
	public boolean isStopping() {
        return checkState(ManagedLifecycleThreadState.STOPPING) ;
    }
    
    /**
     * Check the state against the specified value.
     * @param state The expected state.
     * @return True if the thread is in the expected state, false otherwise.
     */
	private boolean checkState(final ManagedLifecycleThreadState state) {
		
		runningLock.lock();
		try {
			return (this.state == state);
		} finally {
			runningLock.unlock();
		}
	}
    
    /**
     * Set the running state.
     * @param newState The new running state.
     */
	protected void setRunning(final ManagedLifecycleThreadState newState) {
		
        final ManagedLifecycleThreadState origState ;
        runningLock.lock() ;
		try {
			origState = state;
			state = newState;
			runningChanged.signalAll();
		} finally {
			runningLock.unlock();
		}
        fireStateChangedEvent(origState, newState) ;
    }

    /**
     * Wait until the associated thread has changed to a state of "STOPPING".
     * @param terminationPeriod The maximum delay expected for the termination, specified in milliseconds.
     * @return true if the thread changes state to "STOPPING" within the expected period, false otherwise.
     */
	public boolean waitUntilStopping(final long terminationPeriod) {
        return waitForRunningStateChange(ManagedLifecycleThreadState.STOPPING, terminationPeriod) ;
    }

    /**
     * Wait until the associated thread has stopped.
     * @return true if the thread stops within the expected period, false otherwise.
     */
	public boolean waitUntilStopped() {
        return waitUntilStopped(getTerminationPeriod()) ;
    }
    
    /**
     * Wait until the associated thread has stopped.
     * @param terminationPeriod The maximum delay expected for the termination, specified in milliseconds.
     * @return true if the thread stops within the expected period, false otherwise.
     */
	public boolean waitUntilStopped(final long terminationPeriod) {
        return waitForRunningStateChange(ManagedLifecycleThreadState.STOPPED, terminationPeriod) ;
    }
    
    /**
     * Wait until the running state has the specified value.
     * @param state The expected running state value.
     * @param terminationPeriod The maximum delay expected for the termination, specified in milliseconds.
     * @return true if the state has the specified value within the expected period, false otherwise.
     */
    protected boolean waitForRunningStateChange(final ManagedLifecycleThreadState state, final long terminationPeriod) {
		try {
			runningLock.lock();
			try {
				if (this.state != state) {
                    final long end = System.currentTimeMillis() + terminationPeriod ;
					while (this.state != state) {
						final long delay = end - System.currentTimeMillis();
						if (delay <= 0) {
							break;
						}
						runningChanged.await(delay, TimeUnit.MILLISECONDS);
					}
                }
                return (this.state == state) ;
			} finally {
				runningLock.unlock();
			}
		} catch (final InterruptedException ie) {
			if (logger.isInfoEnabled()) {
                logger.info("Interrupted while waiting for running state change") ;
			}

			runningLock.lock();
			try {
				return (this.state == state);
			} finally {
				runningLock.unlock();
			}
		}
	}
    
    /**
     * Add a managed lifecycle thread event listener.
     * @param listener The listener.
     */
    public void addManagedLifecycleThreadEventListener(final ManagedLifecycleThreadEventListener listener) {
        listeners.add(listener) ;
    }
    
    /**
     * Remove a managed lifecycle thread event listener.
     * @param listener The listener.
     */
    public void removeManagedLifecycleThreadEventListener(final ManagedLifecycleThreadEventListener listener) {
        listeners.remove(listener) ;
    }
    
    /**
     * Fire the state changed event.
     * @param origState The original state, prior to transition
     * @param newState The new state after transition
     */
    private void fireStateChangedEvent(final ManagedLifecycleThreadState origState, final ManagedLifecycleThreadState newState) {
		if (listeners.size() > 0) {
            final ManagedLifecycleThreadStateEvent event = new ManagedLifecycleThreadStateEvent(this, origState, newState) ;
            for(ManagedLifecycleThreadEventListener listener: listeners) {
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
		in.defaultReadObject();
		state = ManagedLifecycleThreadState.STOPPED;
	}
}
