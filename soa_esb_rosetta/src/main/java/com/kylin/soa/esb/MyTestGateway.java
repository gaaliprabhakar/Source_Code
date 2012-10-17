package com.kylin.soa.esb;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.lifecycle.AbstractThreadedManagedLifecycle;
import org.jboss.soa.esb.listeners.lifecycle.ManagedLifecycleException;


public class MyTestGateway extends AbstractThreadedManagedLifecycle{

	private static final long serialVersionUID = -4829704220035803933L;
	
	private static final Logger logger = Logger.getLogger(MyTestGateway.class);

	public MyTestGateway(ConfigTree config) throws ConfigurationException {
		super(config);
		
		logger.info("MyTestGateway constructed, ConfigTree: \n" + config.getWholeText());
	}

	protected void doRun() {
		
		logger.info("do Run");
	}

	protected void doInitialise() throws ManagedLifecycleException {
		
		logger.info("do Initialise");
	}

}
