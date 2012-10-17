package org.jboss.soa.esb.actions;

import org.jboss.soa.esb.message.Message;


public abstract class AbstractActionPipelineProcessor extends AbstractActionLifecycle implements ActionPipelineProcessor {
	
    /**
     * Process an exception generated by the pipeline processing.
     * Invoked when the processing of a subsequent stage of the
     * pipeline generates an exception.
     * 
     * @param message The original message.
     * @param th The throwable raised by the pipeline processing
     */
	public void processException(final Message message, final Throwable th) {
    }
    
    /**
     * Process a successful pipeline process. 
     * Invoked when the pipeline processing completes successfully.
     * 
     * @param message The original message.
     */
	public void processSuccess(final Message message) {
	}
}
