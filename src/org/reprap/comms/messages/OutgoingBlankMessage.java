package org.reprap.comms.messages;

import org.reprap.comms.OutgoingMessage;

/**
 *
 */
public class OutgoingBlankMessage extends OutgoingMessage {
	
	/**
	 * 
	 */
	private byte messageType;
	
	/**
	 * @param messageType
	 */
	public OutgoingBlankMessage(byte messageType) {
		this.messageType = messageType;
	}

	/* (non-Javadoc)
	 * @see org.reprap.comms.OutgoingMessage#getBinary()
	 */
	public byte[] getBinary() {
		return new byte [] { messageType };
	}

}
