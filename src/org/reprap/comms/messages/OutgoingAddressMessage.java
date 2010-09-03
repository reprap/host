package org.reprap.comms.messages;

import org.reprap.comms.Address;
import org.reprap.comms.OutgoingMessage;

/**
 *
 */
public class OutgoingAddressMessage extends OutgoingMessage {

	/**
	 * 
	 */
	private byte messageType;
	
	/**
	 * 
	 */
	private byte [] value;
	
	/**
	 * @param messageType
	 * @param address
	 */
	public OutgoingAddressMessage(byte messageType, Address address) {
		this.messageType = messageType;
		this.value = address.getBinary();
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.comms.OutgoingMessage#getBinary()
	 */
	public byte[] getBinary() {
		byte [] payload = new byte[1 + value.length];
		payload[0] = messageType;
		for(int i = 0; i < value.length; i++)
			payload[i+1] = value[i];
		return payload;
	}

	
}
