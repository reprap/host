//package org.reprap.comms.snap;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import gnu.io.CommPortIdentifier;
//import gnu.io.NoSuchPortException;
//import gnu.io.PortInUseException;
//import gnu.io.SerialPort;
//import gnu.io.UnsupportedCommOperationException;
//
//import org.reprap.Device;
//import org.reprap.utilities.Debug;
//import org.reprap.comms.Address;
//import org.reprap.comms.Communicator;
//import org.reprap.comms.IncomingContext;
//import org.reprap.comms.IncomingMessage;
//import org.reprap.comms.OutgoingMessage;
//import org.reprap.Preferences;
//
///**
// *
// */
//public class SNAPCommunicator implements Communicator {
//
//	/**
//	 * Timeout in milliseconds before a timeout exception is thrown
//	 * when waiting for an ACK from a device
//	 */
//	private final static int ackTimeout = 300; // Reduced from 1000...
//	
//	/**
//	 * 
//	 */
//	private final static int messageTimeout = 400;  // Ditto from 800...
//    
//	/**
//	 * 
//	 */
//	private Address localAddress;
//	
//	/**
//	 * Serial port used for comms
//	 * Controlled via the properties (@link) 
//	 */
//	private SerialPort port;
//	
//	/**
//	 * 
//	 */
//	private OutputStream writeStream;
//	
//	/**
//	 * 
//	 */
//	private InputStream readStream;
//	
//	//private ReceiveThread receiveThread = null;
//	
//	
//	/**
//	 * Lock for comms
//	 * @link CommsLock
//	 */
//	private CommsLock lock = new CommsLock();
//		
//	/**
//	 * Construct a new SNAP communicator
//	 * @param portName port used for comms
//	 * @param baudRate Speeds of communication (set via properties @link??)
//	 * @param localAddress 
//	 * @throws NoSuchPortException exception thrown when the port does not exist @see
//	 * @throws PortInUseException exception thrown when the port is in use @see
//	 * @throws IOException
//	 * @throws UnsupportedCommOperationException
//	 */
//	public SNAPCommunicator(String portName, Address localAddress)
//			throws NoSuchPortException, PortInUseException, IOException, UnsupportedCommOperationException {
//
//		this.localAddress = localAddress;
//		Debug.d("Opening port " + portName);
//		CommPortIdentifier commId = CommPortIdentifier.getPortIdentifier(portName);
//		port = (SerialPort)commId.open(portName, 30000);
//		int baudRate = Preferences.loadGlobalInt("BaudRate");
//		
//		
//		// Workround for javax.comm bug.
//		// See http://forum.java.sun.com/thread.jspa?threadID=673793
//		// FIXME: jvandewiel: is this workaround also needed when using the RXTX library?
//		try {
//			port.setSerialPortParams(baudRate,
//					SerialPort.DATABITS_8,
//					SerialPort.STOPBITS_1,
//					SerialPort.PARITY_NONE);
//		}
//		catch (Exception e) {
//			
//		}
//			 
//		port.setSerialPortParams(baudRate,
//				SerialPort.DATABITS_8,
//				SerialPort.STOPBITS_1,
//				SerialPort.PARITY_NONE);
//		
//		// End of workround
//		
//		try {
//			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
//		} catch (Exception e) {
//			// Um, Linux USB ports don't do this. What can I do about it?
//		}
//
//		writeStream = port.getOutputStream();
//		readStream = port.getInputStream();
//
//		Debug.d("Attempting to initialize Arduino");
//        try {Thread.sleep(2000);} catch (Exception e) {}
//		for(int i = 0; i < 10; i++)
//               writeStream.write('0');
//        try {Thread.sleep(1000);} catch (Exception e) {}
//	}
//	
//	public void close()
//	{
//		if (port != null)
//			port.close();
//		port = null;
//	}
//	
//	private String dumpPacket(Device device, OutgoingMessage messageToSend) {
//		byte [] binaryMessage = messageToSend.getBinary();
//		String r = localAddress.toString();
//		r += "->";
//		r += device.getAddress().toString();
//		r += ": ";
//		String rDec = " ( = ";
//		for(int i = 0; i < binaryMessage.length; i++)
//		{
//			r += Integer.toHexString(binaryMessage[i]>=0?binaryMessage[i]:binaryMessage[i]+256) + " ";
//		    rDec += Integer.toString(binaryMessage[i]>=0?binaryMessage[i]:binaryMessage[i]+256) + " ";
//		}
//		return r + rDec + ")";
//	}
//	
//	public IncomingContext sendMessage(Device device,
//			OutgoingMessage messageToSend) throws IOException {
//		
//		byte [] binaryMessage = messageToSend.getBinary(); 
//		SNAPPacket packet = new SNAPPacket((SNAPAddress)localAddress,
//				(SNAPAddress)device.getAddress(),
//				binaryMessage);
//		
//		int tryCount = 0;
//		for(;;) 
//		{
//			Debug.c("tx " +	dumpPacket(device, messageToSend));
//
//			sendRawMessage(packet);
//
//			SNAPPacket ackPacket = null;
//			
//			try {
//				ackPacket = receivePacket(ackTimeout);	
//			} catch (IOException ex) 
//			{
//				tryCount++;
//				Debug.d("Receive error, re-sending: " + ex.getMessage() + "; try: " + tryCount + "; " + 
//						dumpPacket(device, messageToSend));
//				if(tryCount < 16)
//					continue;
//				else
//					ackPacket = null;
//			}
//			if(ackPacket == null)
//				throw new IOException("Resend count exceeded.");
//			if (ackPacket.isAck())
//				break;
//			if (ackPacket.getSourceAddress().equals(localAddress)) {
//				// Packet was from us, so assume no node present
//				Debug.d("Device at address " + device.getAddress() + " not present");
//				throw new IOException("Device at address " + device.getAddress() + " not present");
//			}
//			if (!ackPacket.isNak()) {
//				System.err.println("Received data packet when expecting ACK");
//			}
//
//			// All gone wrong - wait a bit and try again - ***AB
//			System.err.println("sendMessage error - retrying");
//			try
//			{
//				Thread.sleep(100);
//			} catch (Exception e)
//			{
//				System.err.println("sendMessage error" + e.toString());
//			}
//		}
//		
//		IncomingContext replyContext = messageToSend.getReplyContext(this,
//				device);
//		return replyContext;
//	}
//	
//	private synchronized void sendRawMessage(SNAPPacket packet) throws IOException {
////		try{
////			Thread.sleep(200);
////		} catch (Exception ex)
////		{
////			System.err.println("Comms sleep: " + ex.toString());
////		}
//		writeStream.write(packet.getRawData());
//	}
//
//	private int readByte(long timeout) throws IOException {
//		long t0 = System.currentTimeMillis();
//		int c = -1;
//
//		// Sometimes javacomm seems to freak out and say something
//		// timed out when it didn't, so double check and try again
//		// if it really didn't time out
//		for(;;) {
//			c = readStream.read();
//			if (c != -1)
//				return c;
//			if (System.currentTimeMillis() - t0 >= timeout)
//				return -1;
//			
//			try {
//				// Just to avoid a deadly spin if something unexpected happens
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//			}
//		} 
//	}
//	
//	protected synchronized SNAPPacket receivePacket(long timeout) throws IOException {
//		SNAPPacket packet = null;
//		try {
//			port.enableReceiveTimeout(messageTimeout);
//		} catch (UnsupportedCommOperationException e) {
//			Debug.d("Read timeouts unsupported on this platform");
//		}
//		String debugMsg = "debug";
//		String msgHex = "rx: ";
//		String msgDec = " ( = ";
//		Boolean debug = false;
//
//		for(;;) {
//			int c = readByte(timeout);
//			
//			if (debug == true)
//			{
//				if (c == '\n')
//				{
//					Debug.c(debugMsg);
//					debug = false;
//				}
//				else
//					debugMsg += (char)c;
//			}
//			else
//			{
//				msgDec += Integer.toString(c) + " ";
//				msgHex += Integer.toHexString(c) + " ";
//				
//				if (c == -1)
//					throw new IOException("Timeout receiving byte");
//					
//				if (packet == null) {
//
//					if (c != 0x54)  // Always wait for a sync byte before doing anything
//					{
//						if (c == 'd')
//						{
//							debug = true;
//							debugMsg = "From firmware: ";
//							msgHex = "rx: ";
//							msgDec = " ( = ";
//						}
//						continue;
//					}
//					else
//						packet = new SNAPPacket();
//				}
//				if (packet.receiveByte((byte)c)) {
//					// Packet is complete
//					if (packet.validate()) {
//						Debug.c(msgHex + msgDec + ")");
//						return packet;
//					} else {
//						System.err.println("CRC error");
//						throw new IOException("CRC error");
//					}
//				}
//			}
//		}	
//	}
//	
//	public void receiveMessage(IncomingMessage message) throws IOException {
//		receiveMessage(message, messageTimeout);
//	}
//	
//	public void receiveMessage(IncomingMessage message, long timeout) throws IOException {
//		// Here we collect one packet and notify the message
//		// of its contents.  The message will respond
//		// to indicate if it wants the message.  If not,
//		// it will be discarded and we will wait for another
//		// message.
//		
//		// Since this is a SNAP ring, we have to pass on
//		// any packets that are not destined for us.
//		
//		// We will also only pass packets to the message if they are for
//		// the local address.
//		for(;;) {
//			SNAPPacket packet = receivePacket(timeout);
//			if (processPacket(message, packet))
//				return;
//		}
//	}
//	
//	private boolean processPacket(IncomingMessage message, SNAPPacket packet) throws IOException {
//		// First ACK the message
//		if (packet.isAck()) {
//			Debug.d("Unexpected ACK received instead of message, not supported yet");
//	  	  	return false;
//		}
//		/// TODO send ACKs
//		//sendRawMessage(packet.generateACK());
//		
//		if (!packet.getDestinationAddress().equals(localAddress)) {
//			// Not for us, so forward it on
//			sendRawMessage(packet);
//			return false;
//		} else if (message.receiveData(packet.getPayload())) {
//			// All received as expected
//			return true;
//		} else {
//			// Not interested, wait for more
//			Debug.d("Ignored and dropped packet");
//			return false;
//		}
//	}
//
//	public Address getAddress() {
//		return localAddress;
//	}
//
//	public void dispose() {
//		close();
//	}
//
//	public void lock() {
//		lock.lock();
//	}
//
//	public void unlock() {
//		lock.unlock();
//	}
//	
//	// TODO make a background receiver thread.  It can keep a pool of async receive contexts and
//	// fire them off if anything matching arrives.
//	
//	// TODO Make a generic message receiver.  Use reflection to get correct class. 
//
//}
