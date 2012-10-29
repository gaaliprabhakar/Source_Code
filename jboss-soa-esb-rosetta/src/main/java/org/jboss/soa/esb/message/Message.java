package org.jboss.soa.esb.message;

import java.io.IOException;
import java.net.URI;

/**
 * This is the basic internal core message abstraction. A message consists of the following
 * components:
 * 
 * Header: the header information contains information such as the destination EPR, the
 * sender EPR, where the reply goes etc, i.e., general message-level functional information.
 * Context: additional information to contextualise the message; for example, transaction or
 * security data, the identity of the ultimate receiver, or HTTP-cookie like information.
 * Body: the actual payload of the message.
 * Fault: any fault information associated with the message.
 * Attachment: any attachments associated with the message.
 * Properties: any message specific properties.
 * 
 * Each message, once created, has a corresponding element for these 5 components. That element
 * may be empty (<b>NOT NULL</b>). The object representing the element can then be used to act
 * on the corresponding data item in the message.
 * 
 *
 */

public interface Message {
	
	/**
	 * @return get the header component of the message.
	 */
	public Header getHeader ();

	/**
	 * @return get the context component of the message.
	 */
	public Context getContext ();
	
	/**
	 * @return get the body component of the message.
	 */
	public Body getBody ();

	/**
	 * @return get any faults associated with the message. These should not be application level faults, but comms level.
	 */
	public Fault getFault ();
	
	/**
	 * @return get any message attachments.
	 */
	public Attachment getAttachment ();
	
	/**
	 * @return the type of this message.
	 */
	public URI getType ();
	
	/**
	 * @return Properties - any message properties.
	 */
	public Properties getProperties ();

	/**
	 * @return a duplicate of this instance. Just serialize and then deserialize.
	 */
    public Message copy () throws IOException;
}
