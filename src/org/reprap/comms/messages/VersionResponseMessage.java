package org.reprap.comms.messages;

import java.io.IOException;

import org.reprap.comms.IncomingContext;
import org.reprap.comms.IncomingMessage;

/**
 *
 */
public class VersionResponseMessage extends IncomingMessage {

	/**
	 * @param incomingContext
	 * @throws IOException
	 */
	public VersionResponseMessage(IncomingContext incomingContext) throws IOException {
		super(incomingContext);
	}
	
	/**
	 * @return version number
	 * @throws InvalidPayloadException
	 */
	public int getVersion() throws InvalidPayloadException {
	    byte [] reply = getPayload();
	    if (reply == null || reply.length != 3)
	    	throw new InvalidPayloadException();
//	    System.out.println("ver: " + reply[0] + ", "
//	    		+ reply[1] + ", "
//	    		+ reply[2]);
	    return reply[2] + (reply[1] << 8);
	}

	/* (non-Javadoc)
	 * @see org.reprap.comms.IncomingMessage#isExpectedPacketType(byte)
	 */
	protected boolean isExpectedPacketType(byte packetType) {
		return packetType == VersionRequestMessage.MSG_GetVersion;
	}

}
