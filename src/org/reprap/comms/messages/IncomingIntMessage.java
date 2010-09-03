package org.reprap.comms.messages;

import java.io.IOException;

import org.reprap.Device;
import org.reprap.comms.IncomingContext;
import org.reprap.comms.IncomingMessage;
import org.reprap.comms.OutgoingMessage;

/**
 *
 */
public abstract class IncomingIntMessage extends IncomingMessage {

	/**
	 * @param incomingContext
	 * @throws IOException
	 */
	public IncomingIntMessage(IncomingContext incomingContext)
			throws IOException {
		super(incomingContext);
	}

	/**
	 * @param device
	 * @param message
	 * @param timeout
	 * @throws IOException
	 */
	public IncomingIntMessage(Device device, OutgoingMessage message, long timeout)
		throws IOException
	{
		super(device, message, timeout);
	}
	
	/**
	 * @param b1
	 * @param b2
	 * @return integer
	 */
	public static int ConvertBytesToInt(byte b1, byte b2) {
		int low = b1;
		int high = b2;
	    if (low < 0) low += 256;
	    if (high < 0) high += 256;
	    return low + (high << 8);
	}
	
	/**
	 * @return value
	 * @throws InvalidPayloadException
	 */
	public int getValue() throws InvalidPayloadException {
	    byte [] reply = getPayload();
	    if (reply == null || reply.length != 3)
	    	throw new InvalidPayloadException();
	    return ConvertBytesToInt(reply[1], reply[2]);
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.comms.IncomingMessage#isExpectedPacketType(byte)
	 */
	abstract protected boolean isExpectedPacketType(byte packetType);

}
