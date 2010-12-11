//package org.reprap.comms;
//
//import org.reprap.Device;
//
///**
// *
// */
//public abstract class OutgoingMessage {
//		
//	/**
//	 * Fetch the binary payload corresponding to the completed message
//	 * @return an array of bytes
//	 */
//	public abstract byte [] getBinary();
//	
//	/**
//	 * Return an IncomingContext for a message in response to
//	 * this message.  If necessary this can be overridden to
//	 * include sequence numbers or other disambiguating
//	 * information.
//	 * @param communicator
//	 * @return An IncomingContext object that can be used to
//	 *   receive the reply to the message.
//	 */
//	public IncomingContext getReplyContext(Communicator communicator,
//			Device device) {
//		return new IncomingContext(communicator, device.getAddress());
//	}
//}
