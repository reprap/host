package org.reprap.comms.messages;

import org.reprap.comms.OutgoingMessage;

/**
 *
 */
public class OutgoingIntMessage extends OutgoingMessage {

	/**
	 * 
	 */
	private byte messageType;
	
	/**
	 * 
	 */
	private int value;
	
	/**
	 * @param messageType
	 * @param value
	 */
	public OutgoingIntMessage(byte messageType, int value) {
		this.messageType = messageType;
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.comms.OutgoingMessage#getBinary()
	 */
	public byte[] getBinary() {
		byte [] payload = new byte[3];
		payload[0] = messageType;
		payload[1] = (byte)(value & 255);
		payload[2] = (byte)((value >> 8) & 255);
		return payload;
	}

	
}
