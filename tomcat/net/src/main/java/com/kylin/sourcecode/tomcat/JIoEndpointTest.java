package com.kylin.sourcecode.tomcat;

import org.apache.tomcat.util.net.JIoEndpoint;

public class JIoEndpointTest {

	public static void main(String[] args) throws Exception {

		JIoEndpoint endpoint = new JIoEndpoint();
		endpoint.init();
		endpoint.start();
	}

}
