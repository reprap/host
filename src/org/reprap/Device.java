package org.reprap;

import org.reprap.Printer;
import org.reprap.comms.Address;
import org.reprap.comms.Communicator;

/**
 * Class implements an abstract device containing the basic properties and methods.
 * An "implemented" device refers to for example a UCB/Stepper motor combination, 
 * extruder or other. 
 */
public class Device {

	/**
	 * Adress of the device. Identifier returned by the firmware in the device
	 */
	private Address address;
	
	/**
	 * Result of the last call to isAvailable (which we don't want to call in loops
	 * etc as it does real comms)
	 */
//	private boolean wasAlive = false; // commented out as isAvailable is commented
	
	/**
	 * Communicator
	 * 
	 */
	private Communicator communicator = org.reprap.Main.getCommunicator();
	
	/**
	 * To whom (grammar) do I belong?
	 * if null, device is a brain in a bottle 
	 * (i.e. just working alone on the bench).
	 */
	public Printer printer = null;

	/**
	 * Basic constructor for a device.
	 * @param communicator communicator used by the device
	 * @param address address of the device
	 */
	public Device(Communicator communicator, Address address) {
		this.communicator = communicator;
		this.address = address;
		//isAvailable();
	}
	
	public Device(Address address)
	{
		this.address = address;
		//isAvailable();		
	}

	/**
	 * @return the adress of the device
	 */
	public Address getAddress() {
		return address;
	}


	
	/**
	 * Check if the device is alive
	 * @return
	 */
//	public boolean isAvailable()
//	{		
//	       try {
//	            getVersion();
//	        } catch (Exception ex) {
//	        	wasAlive = false;
//	            return false;
//	        }
//	        wasAlive = true;
//	        return true;
//	}
	
	/**
	 * Result of last call to isAvailable(), which we don't want to
	 * call repeatedly as each call polls the device.
	 * @return
	 */
//	public boolean wasAvailable()
//	{		
//		return wasAlive;
//	}
	
	//*****************************************************************
	
	/**
	 * @return the communicator
	 */
	public Communicator getCommunicator() {
		return communicator;
	}
//
//	/**
//	 * @return Version ID of the firmware the device is running  
//	 * @throws IOException
//	 * @throws InvalidPayloadException
//	 */
//	public int getVersion() throws IOException, InvalidPayloadException {
//		VersionRequestMessage request = new VersionRequestMessage();
//		IncomingContext replyContext = sendMessage(request);
//		VersionResponseMessage reply = new VersionResponseMessage(replyContext);
//		return reply.getVersion(); 
//	}
//	
//	/**
//	 * @param message 
//	 * @return incoming context
//	 * @throws IOException
//	 */
//	public IncomingContext sendMessage(OutgoingMessage message) throws IOException {
//		return communicator.sendMessage(this, message);
//	}
//	
//	/**
//	 * Method to lock communication to this device. 
//	 * <p>TODO: when called?</P> 
//	 */
//	public void lock() {
//		communicator.lock();
//	}
//	
//	/**
//	 * Method to unlock communication to this device
//	 * <p>TODO: when called?</P> 
//	 *  
//	 */
//	public void unlock() {
//		communicator.unlock();
//	}
//	
	//******************************************************************
	
	public void setPrinter(Printer p)
	{
		printer = p;
	}
	public Printer getPrinter()
	{
		return printer;
	}	
}
