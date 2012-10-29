package org.jboss.soa.esb.listeners.lifecycle;

/**
 * This enumeration represents the lifecycle state of a managed instance.
 * <p/>
 * Allowable transitions are as follows
 * <table border="1">
 * <thead>
 * <tr><th>Originating state</th><th>New states</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>CONSTRUCTED</td><td>INITIALISING</td></tr>
 * <tr><td>INITIALISING</td><td>INITIALISED, DESTROYED</td></tr>
 * <tr><td>INITIALISED</td><td>STARTING, DESTROYED</td></tr>
 * <tr><td>STARTING</td><td>STARTED, STOPPED</td></tr>
 * <tr><td>STARTED</td><td>RUNNING, STOPPING</td></tr>
 * <tr><td>RUNNING</td><td>STOPPING</td></tr>
 * <tr><td>STOPPING</td><td>STOPPED</td></tr>
 * <tr><td>STOPPED</td><td>STARTING, DESTROYING</td></tr>
 * <tr><td>DESTROYING</td><td>DESTROYED</td></tr>
 * </tbody>
 * </table>
 * 
 */
public enum ManagedLifecycleState {
	
	CONSTRUCTED {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return (newState == INITIALISING);
		}
	},
	INITIALISING {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return ((newState == INITIALISED) || (newState == DESTROYED));
		}
	},
	INITIALISED {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return ((newState == STARTING) || (newState == DESTROYING));
		}
	},
	STARTING {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return ((newState == STARTED) || (newState == STOPPED));
		}
	},
	STARTED {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return ((newState == RUNNING) || (newState == STOPPING));
		}
	},
	RUNNING {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return (newState == STOPPING);
		}
	},
	STOPPING {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return (newState == STOPPED);
		}
	},
	STOPPED {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return ((newState == STARTING) || (newState == DESTROYING));
		}
	},
	DESTROYING {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return (newState == DESTROYED);
		}
	},
	DESTROYED {
		public boolean canTransition(final ManagedLifecycleState newState) {
			return false;
		}
	};
    
    public abstract boolean canTransition(final ManagedLifecycleState newState) ;
}
