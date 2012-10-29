package org.jboss.test.remoting;

import javax.management.MBeanServer;

import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;

public class JBossRemotingCLientInvocationHandler implements ServerInvocationHandler {

	public void addListener(InvokerCallbackHandler arg0) {

	}

	public Object invoke(InvocationRequest invocation) throws Throwable {
		return invocation.getParameter();
	}

	public void removeListener(InvokerCallbackHandler arg0) {

	}

	public void setInvoker(ServerInvoker arg0) {

	}

	public void setMBeanServer(MBeanServer arg0) {

	}

}
