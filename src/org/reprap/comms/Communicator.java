//package org.reprap.comms;
//
//import java.io.IOException;
//
//import org.reprap.Device;
//import org.reprap.comms.OutgoingMessage;
//
///**
// *
// */
//public interface Communicator {
//	
//	/**
//	 * @param device
//	 * @param messageToSend
//	 * @return IncomingContext
//	 * @throws IOException
//	 */
//	public IncomingContext sendMessage(Device device,
//			OutgoingMessage messageToSend) throws IOException;
//
//	/**
//	 * @param message
//	 * @throws IOException
//	 */
//	public void receiveMessage(IncomingMessage message) throws IOException;
//	
//	/**
//	 * @param message
//	 * @param timeout
//	 * @throws IOException
//	 */
//	public void receiveMessage(IncomingMessage message, long timeout) throws IOException;
//	
//	/**
//	 * 
//	 */
//	public void close();
//	
//	/**
//	 * 
//	 */
//	public void dispose();
//	
//	/**
//	 * @return Address
//	 */
//	public Address getAddress();
//	
//	// These methods will be removed when the asynchronous contexts are completed
//	/**
//	 * 
//	 */
//	public void lock();
//	
//	/**
//	 * 
//	 */
//	public void unlock();
//}
