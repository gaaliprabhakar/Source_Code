package org.jboss.test.remoting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvocationResponse;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.transport.Connector;
import org.jboss.remoting.transport.PortUtil;

public class JBossRemotingClient {
	
	private static final Logger logger = Logger.getLogger(JBossRemotingClient.class);
	
	protected String host;
	protected int port;
	protected String locatorURI;
	protected InvokerLocator serverLocator;
	protected Connector connector;
	protected JBossRemotingCLientInvocationHandler invocationHandler;
	
	public JBossRemotingClient() {
		
		setupLogger();
	}

	private void setupServer() throws Exception {
		
		host = InetAddress.getLocalHost().getHostAddress();
		port = PortUtil.findFreePort(host);
		locatorURI = getTransport() + "://" + host + ":" + port;
		serverLocator = new InvokerLocator(locatorURI);
		logger.info("Starting remoting server with locator uri of: " + locatorURI);
		HashMap config = new HashMap();
		config.put(InvokerLocator.FORCE_REMOTE, "true");
		connector = new Connector(serverLocator, config);
		connector.create();
		invocationHandler = new JBossRemotingCLientInvocationHandler();
		connector.addInvocationHandler("test", invocationHandler);
		connector.start();
	}
	
	private String getTransport() {
		return "socket";
	}

	private void setupLogger() {

		Logger.getLogger("org.jboss.remoting").setLevel(Level.DEBUG);
        Logger.getLogger("org.jboss.test.remoting").setLevel(Level.INFO);
        String pattern = "[%d{ABSOLUTE}] [%t] %5p (%F:%L) - %m%n";
        PatternLayout layout = new PatternLayout(pattern);
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        Logger.getRootLogger().addAppender(consoleAppender);  
	}
	
	public String getSendData() throws IOException {
		
//		File file = new File("etc/sxCheck.xml");
//		File file = new File("etc/sxCheckEnd.xml");
		File file = new File("etc/sxCheckData.xml");

        String testdata = "";
        
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] data = null;
        byte[] b = new byte[1024];
        int read = 0;
        FileInputStream in = new FileInputStream(file);

        while ( ( read = in.read( b ) ) > 0 )
        {
            byteOut.write( b, 0, read );
        }
        
        data = byteOut.toByteArray();
        in.close();
        testdata = new String( data );
        String packageLength = Integer.toString(testdata.getBytes().length);
        while(packageLength.length()<4){
        	packageLength =  "0"+packageLength ;
        }
        
        return testdata;
	}
	
	/**
	 * Remoting expects a version byte, but, since the payload is not an
	 * InvocationRequest, it doesn't write a version byte.
	 * @throws Exception 
	 */
	public void testRawData() throws Exception {
		
		logger.info("Send Raw data test");
		
		setupServer();
		
		Socket s = new Socket("localhost", serverLocator.getPort());
		
//		for(int i = 0 ; i < 2 ; i ++) {
//			
////			OutputStream out = s.getOutputStream();
////			out.write("22".getBytes());
////			InputStream in = s.getInputStream();
////			
////			byte[] buf = new byte[1024];
////			
////			int count = in.read(buf);
////			
////			String result = new String(buf); 
//			
//			
//		}
		
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.write(22); // Write version byte.
		oos.writeObject(getSendData());
		oos.writeObject(getSendData());
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		Object result = ois.readObject();
		printResult(result);
		
		logger.info("Test Done");
	}
	
	private void printResult(Object result) {

		System.out.println("\n---------------------- Responsed String From JBoss Remoting ---------------------------");
		System.out.println(result);
		System.out.println("---------------------------------------------------------------------------------------\n");
	}

	/**
	 * The payload is an InvocationRequest, so Remoting returns a version byte.
	 */
	public void testInvocationRequest() throws Throwable {
		
		logger.info("entering ");
	      
		setupServer();
		
		Socket s = new Socket("localhost", serverLocator.getPort());
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.write(22); // Write version byte.
		InvocationRequest request = new InvocationRequest(null, "test", "abc", null, null, serverLocator);
		oos.writeObject(request);
		InputStream is = s.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		ois.read(); // Get version byte.
		InvocationResponse response = (InvocationResponse) ois.readObject();
		Object result = response.getResult();
		System.out.println(result);
		
		shutdownServer();
	}

	private void shutdownServer() {

		if (connector != null)
	         connector.stop();
	}

	public static void main(String[] args) throws Exception {

		JBossRemotingClient test = new JBossRemotingClient();
		
		test.testRawData();
	}

}
