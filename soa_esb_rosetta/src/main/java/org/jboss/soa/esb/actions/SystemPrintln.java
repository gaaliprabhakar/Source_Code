package org.jboss.soa.esb.actions;

import java.io.PrintStream;

import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.MessagePayloadProxy;
import org.jboss.soa.esb.message.body.content.BytesBody;
import org.jboss.soa.esb.util.Util;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;

/**
 * Simple action that prints out the message contents using System.println.
 */
public class SystemPrintln extends AbstractActionPipelineProcessor {
	
	public static final String PRE_MESSAGE = "message";
	public static final String FULL_MESSAGE = "printfull";
	public static final String PRINT_STREAM = "outputstream";
    public static final String DEFAULT_PRE_MESSAGE = "Message structure";
    
    private MessagePayloadProxy payloadProxy;
    
    private String printlnMessage;
    
	private boolean printFullMessage;
	
	private boolean useOutputStream;

    /**
	 * Public constructor.
	 * 
	 * If no PRE_MESSAGE data is provided within the supplied ConfigTree instance
	 * then DEFAULT_PRE_MESSAGE is used.
	 * 
	 * @param config
	 *            Configuration.
	 */
	public SystemPrintln(ConfigTree config) {
		
		printlnMessage = config.getAttribute(PRE_MESSAGE, DEFAULT_PRE_MESSAGE);
		printFullMessage = (config.getAttribute(FULL_MESSAGE, "false").equalsIgnoreCase("true") ? true : false);
		useOutputStream = (config.getAttribute(PRINT_STREAM, "true").equals("true") ? true : false);

        String primaryDataLocation = config.getAttribute("datalocation");
        if(primaryDataLocation != null) {
            config.setAttribute(MessagePayloadProxy.GET_PAYLOAD_LOCATION, primaryDataLocation);
            payloadProxy = new MessagePayloadProxy(config);
        } else {
            payloadProxy = new MessagePayloadProxy(config, new String[] {BytesBody.BYTES_LOCATION}, new String[] {BytesBody.BYTES_LOCATION});
        }
        payloadProxy.setNullGetPayloadHandling(MessagePayloadProxy.NullPayloadHandling.LOG);
    }

	public Message process(Message message) throws ActionProcessingException {
		
		System.out.println("Process Message: ");
		System.out.println("Message Type: " + message.getType());
		System.out.println("Message Header: " + message.getHeader());
		System.out.println("Message Body: " + message.getBody());
		System.out.println("Message Content: " + message.getContext());
		System.out.println("Process Message End");
		
        Object messageObject = null;
        try {
            messageObject = payloadProxy.getPayload(message);
        } catch (MessageDeliverException e) {
            throw new ActionProcessingException(e);
        }

		PrintStream stream = (useOutputStream ? System.out : System.err);
		
		stream.println(printlnMessage + ": ");
        
        String messageStr=null;
		
		if (printFullMessage && (message != null)) {
			// the message should be responsible for converting itself to a string
            messageStr = message.toString();
			stream.println("[ "+messageStr+" ]");

		} else {
			if (messageObject instanceof byte[]) {
                messageStr = Util.format(new String((byte[]) messageObject));
				stream.println("[" + messageStr + "].");
			} else {
				if (messageObject != null) {
                    messageStr = Util.format(messageObject.toString());
					stream.println("[" + messageStr + "].");
				}
				for (int i = 0; i < message.getAttachment().getUnnamedCount(); i++) {
					Message attachedMessage = (Message) message.getAttachment().itemAt(i);
                    try {
                        Object payload = payloadProxy.getPayload(attachedMessage);
                        if(payload instanceof byte[]) {
							stream.println("attachment " + i + ": [" + new String((byte[]) payload) + "].");
                        } else {
							stream.println("attachment " + i + ": [" + payload + "].");
                        }
                    } catch (MessageDeliverException e) {
                        throw new ActionProcessingException(e);
                    }
                }
			}
		}
		return message;
	}

	
}
