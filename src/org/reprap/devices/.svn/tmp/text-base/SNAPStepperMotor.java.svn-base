//package org.reprap.devices;
//
//import java.io.IOException;
//import org.reprap.utilities.Debug;
//import org.reprap.Device;
//import org.reprap.Preferences;
//import org.reprap.AxisMotor;
//import org.reprap.ReprapException;
//import org.reprap.comms.Address;
//import org.reprap.comms.Communicator;
//import org.reprap.comms.IncomingContext;
//import org.reprap.comms.OutgoingMessage;
//import org.reprap.comms.IncomingMessage;
//import org.reprap.comms.IncomingMessage.InvalidPayloadException;
//import org.reprap.comms.messages.IncomingIntMessage;
//import org.reprap.comms.messages.OutgoingAddressMessage;
//import org.reprap.comms.messages.OutgoingBlankMessage;
//import org.reprap.comms.messages.OutgoingByteMessage;
//import org.reprap.comms.messages.OutgoingIntMessage;
//import org.reprap.comms.messages.VersionRequestMessage;
//import org.reprap.comms.messages.VersionResponseMessage;
///**
// *
// */
//public class SNAPStepperMotor extends GenericStepperMotor {
//	/**
//	 * Communicator
//	 * 
//	 */
//	private Communicator communicator = null;	
//	/**
//	 * API for firmware
//	 * Activate the stepper motor in forward direction 
//	 */
//	public static final byte MSG_SetForward = 1;
//	
//	/**
//	 * Activate the stepper motor in reverse direction 
//	 */
//	public static final byte MSG_SetReverse = 2;
//	
//	/**
//	 *  Set the stepper motor position (how?)
//	 */
//	public static final byte MSG_SetPosition = 3;
//	
//	/**
//	 * Get the current stepper motor position
//	 */
//	public static final byte MSG_GetPosition = 4;
//	
//	/**
//	 * 
//	 */
//	public static final byte MSG_Seek = 5;	
//	
//	/**
//	 * Set the motor to idle; this turns the torque off whereas speed = 0 keeps torque on 
//	 */
//	public static final byte MSG_SetIdle = 6;		
//	
//	/**
//	 * Set notification (?) 
//	 */
//	public static final byte MSG_SetNotification = 7;
//	
//	/**
//	 * Set the sync mode (?)
//	 */
//	public static final byte MSG_SetSyncMode = 8;
//	
//	/**
//	 * Calibrate (?) 
//	 */
//	public static final byte MSG_Calibrate = 9;
//	
//	/**
//	 * Get the range (?)  
//	 */
//	public static final byte MSG_GetRange = 10;
//	
//	/**
//	 * DDAMaster ?
//	 */
//	public static final byte MSG_DDAMaster = 11;
//	
//	/**
//	 * Move on step in forward direction 
//	 */
//	public static final byte MSG_StepForward = 12;
//	
//	/**
//	 * Move on step in backward direction
//	 */
//	public static final byte MSG_StepBackward = 13;	
//	
//	/**
//	 * Set the power to the stepper motor 
//	 */
//	public static final byte MSG_SetPower = 14;
//	
//	/**
//	 * Homereset(?? 
//	 */
//	public static final byte MSG_HomeReset = 16;
//	
//	/**
//	 * Stick a point in the input buffer (firmware v1.4 and greater)
//	 */
//	public static final byte MSG_QueuePoint = 17;
//	
//	/**
//	 * What's the thing up to?
//	 */
//	public static final byte MSG_GetStatus = 18;
//	
//	/**
//	 * What the firmware sends when the queue is active
//	 */
//	public static final byte modeQueue = 7;	
//
//
//	/**
//	 * 
//	 */
//	public static final byte SYNC_NONE = 0;
//	public static final byte SYNC_SEEK = 1;
//	public static final byte SYNC_INC = 2;
//	public static final byte SYNC_DEC = 3;
//	
//	/**
//	 * 
//	 */
//	private boolean haveInitialised = false;
//	private boolean haveSetNotification = false;
//	private boolean haveCalibrated = false;
//	
//	/**
//	 * Power output limiting (0-100 percent)
//	 */
//	private int maxTorque;
//	
//	
//	/**
//	 * 
//	 */
//	private int firmwareVersion = 0;
//	
//	private static final int firmwareVersionForBuffering = (1 << 8) + 4; // Version 1.4
//	
//	/**
//	* our snap comms device.
//	*/
//	private Device snap;
//	
//	private boolean wasAlive = false;
//	
//	/**
//	 * @param communicator
//	 * @param address
//	 * @param prefs
//	 * @param motorId
//	 */
//	public SNAPStepperMotor(Communicator com, Address address, int motorId) {
//		
//		super(address, motorId);
//		communicator = com;
//		snap = new Device(communicator, address);
//		isAvailable();	
//		refreshPreferences();
//		
//		if(!isAvailable())
//			return;
//		
//		try
//		{
//			firmwareVersion = getVersion();
//		} catch (Exception ex)
//		{}
//
//		Debug.d("Stepper " + axis + " firmware is version: " + 
//				((firmwareVersion >> 8) & 0xff) + "." + (firmwareVersion & 0xff));
//
//	}
//	
//	
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
//	
//	/**
//	 * Result of last call to isAvailable(), which we don't want to
//	 * call repeatedly as each call polls the device.
//	 * @return
//	 */
//	public boolean wasAvailable()
//	{		
//		return wasAlive;
//	}	
//	
//	
//	/**
//	 * @return the communicator
//	 */
//	public Communicator getCommunicator() {
//		return communicator;
//	}
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
//	//******************************************************************
//	
//	public void refreshPreferences()
//	{
//		try
//		{
//			this.maxTorque = Preferences.loadGlobalInt(axis + "AxisTorque(%)");
//		} catch (Exception ex)
//		{
//			System.err.println("Refresh motor preferences: " + ex.toString());
//		}
//		
//	}
//
//	/**
//	 * @throws IOException
//	 */
//	private void initialiseIfNeeded() throws IOException {
//		if (!haveInitialised) {
//			haveInitialised = true;
//			setMaxTorque(maxTorque);
//		}
//	}
//	
//	/**
//	 * Dispose of this object
//	 */
//	public void dispose() {
//	}
//	
//	
//	private int getStatus() throws IOException
//	{
//		int value;
//		communicator.lock();
//		try {
//			IncomingContext replyContext = communicator.sendMessage(snap,
//					new OutgoingBlankMessage(MSG_GetStatus));
//			
//			IncomingIntMessage reply = new RequestStatusResponse(replyContext);
//			try {
//				value = reply.getValue();
//			}
//			catch (IncomingMessage.InvalidPayloadException ex) {
//				throw new IOException(ex.getMessage());
//			}
//		}
//		finally {
//			communicator.unlock();
//		}
//		//System.out.println("status: " + (byte)(value >> 8) + ", " + (byte)(value & 0xff));
//		return value;
//	}
//	
//	/**
//	 * 
//	 *
//	 */
//	private void waitTillQueueNotFull() throws IOException
//	{
//		if(firmwareVersion < firmwareVersionForBuffering)
//			return;
//		
//		int value = getStatus();
//		
//		while((value >> 8) != 0)
//		{
//			try {
//				printer.machineWait(500, false);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			value = getStatus();
//		}
//	}
//	
//	/**
//	 * 
//	 *
//	 */
//	public void waitTillNotBusy() throws IOException
//	{
//		if(firmwareVersion < firmwareVersionForBuffering)
//			return;	
//		
//		int value = getStatus();
//		
//		while((value & 0xff) == modeQueue)
//		{
//			//System.out.println("busy: " + value);
//			try {
//				printer.machineWait(500, false);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			value = getStatus();
//		}
//	}	
//	
//	/**
//	 * Add an XY point to the firmware buffer for plotting
//	 * Only works for recent firmware.
//	 * 
//	 * @param endX
//	 * @param endY
//	 * @param movementSpeed
//	 * @return
//	 */
//	public boolean queuePoint(int endX, int endY, int movementSpeed, int control) throws IOException 
//	{
//		// Firmware clever enough?
//		if(firmwareVersion < firmwareVersionForBuffering)
//			return false;
//		
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return false;
//		}
//		
//		initialiseIfNeeded();
//		
//		waitTillQueueNotFull();
//		//System.out.println("control: " + control);
//		communicator.lock();
//		Debug.d(axis + " axis - queuing point at speed " + movementSpeed + ", control = " + control + ", endX = " + endX + ", endY = " + endY);
//		try {
//			OutgoingMessage request = new RequestQueue(endX, endY, movementSpeed, control);
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//		
//		return true;
//	}
//	
//	/**
//	 * Set the motor speed (or turn it off) 
//	 * @param speed A value between -255 and 255.
//	 * @throws ReprapException
//	 * @throws IOException
//	 */
//	public void setSpeed(int speed) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - setting speed: " + speed);
//		try {
//			OutgoingMessage request = new RequestSetSpeed(speed);
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//
//	/**
//	 * @throws IOException
//	 */
//	public void setIdle() throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - going idle.");
//		try {
//			OutgoingMessage request = new RequestSetSpeed();
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	public void stepForward() throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - stepping forward.");		
//		try {
//			OutgoingMessage request = new RequestOneStep(true);
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	public void stepBackward() throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - stepping backward.");	
//		try {
//			OutgoingMessage request = new RequestOneStep(false);
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	public void resetPosition() throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		setPosition(0);
//	}
//	
//	/**
//	 * @param position
//	 * @throws IOException
//	 */
//	public void setPosition(int position) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - setting position to: " + position);	
//		try {
//			communicator.sendMessage(snap, new RequestSetPosition(position));
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @return current position of the motor
//	 * @throws IOException
//	 */
//	public int getPosition() throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return 0;
//		}
//		int value;
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - getting position.  It is... ");
//		try {
//			IncomingContext replyContext = communicator.sendMessage(snap, 
//					new OutgoingBlankMessage(MSG_GetPosition));
//			
//			IncomingIntMessage reply = new RequestPositionResponse(replyContext);
//			try {
//				value = reply.getValue();
//			}
//			catch (IncomingMessage.InvalidPayloadException ex) {
//				throw new IOException(ex.getMessage());
//			}
//		}
//		finally {
//			communicator.unlock();
//		}
//		Debug.d("..." + value);		
//		return value;
//	}
//	
//	/**
//	 * @param speed
//	 * @param position
//	 * @throws IOException
//	 */
//	public void seek(int speed, int position) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - seeking position " + position + " at speed " + speed);
//		try {
//			communicator.sendMessage(snap, new RequestSeekPosition(speed, position));
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//
//	/**
//	 * @param speed
//	 * @param position
//	 * @throws IOException
//	 */
//	public void seekBlocking(int speed, int position) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - seeking-blocking position " + position + " at speed " + speed);
//		try {
//			setNotification();
//			new RequestSeekResponse(snap, new RequestSeekPosition(speed, position), 50000);
//			setNotificationOff();
//		} catch (Exception e) {
//			// TODO: Nasty error. But WTF do we do about it?
//			e.printStackTrace();
//		} finally {
//			communicator.unlock();
//		}
//	}
//
//	/**
//	 * @param speed
//	 * @return range of the motor
//	 * @throws IOException
//	 * @throws InvalidPayloadException
//	 */
//	public AxisMotor.Range getRange(int speed) throws IOException, InvalidPayloadException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return new AxisMotor.Range();
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - getting range.");
//		try {
//			if (haveCalibrated) {
//				IncomingContext replyContext = communicator.sendMessage(snap,
//						new OutgoingBlankMessage(MSG_GetRange));
//				RequestRangeResponse response = new RequestRangeResponse(replyContext);
//				return response.getRange();
//			} else {
//				setNotification();
//				IncomingContext replyContext = communicator.sendMessage(snap,
//						new OutgoingByteMessage(MSG_Calibrate, (byte)speed));
//				RequestRangeResponse response = new RequestRangeResponse(replyContext);
//				setNotificationOff();
//				return response.getRange();
//			}
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @param speed
//	 * @throws IOException
//	 * @throws InvalidPayloadException
//	 */
//	public void homeReset(int speed) throws IOException, InvalidPayloadException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - home reset at speed " + speed);
//		try {
//			setNotification();
//			new RequestHomeResetResponse(snap, new OutgoingByteMessage(MSG_HomeReset, (byte)speed), 60000);
//			setNotificationOff();
//		} finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @param syncType
//	 * @throws IOException
//	 */
//	public void setSync(byte syncType) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - setting sync to " + syncType);
//		try {
//			communicator.sendMessage(snap,
//					new OutgoingByteMessage(MSG_SetSyncMode, syncType));
//		}
//		finally {
//			communicator.unlock();
//		}
//		
//	}
//	
//	/**
//	 * @param speed
//	 * @param x1
//	 * @param deltaY
//	 * @throws IOException
//	 */
//	public void dda(int speed, int x1, int deltaY) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		communicator.lock();
//		Debug.d(axis + " axis - dda at speed " + speed + ". x1 = " + x1 + ", deltaY = " + deltaY);
//		try {
//			setNotification();
//			
//			new RequestDDAMasterResponse(snap, new RequestDDAMaster(speed, x1, deltaY), 60000);
//			
//			setNotificationOff();
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	private void setNotification() throws IOException {
//		
//		initialiseIfNeeded();
//		Debug.d(axis + " axis - setting notification on.");
//		if (!haveSetNotification) {
//			communicator.sendMessage(snap, new OutgoingAddressMessage(MSG_SetNotification,
//					communicator.getAddress()));
//			haveSetNotification = true;
//		}
//	}
//
//	/**
//	 * @throws IOException
//	 */
//	private void setNotificationOff() throws IOException {
//		initialiseIfNeeded()	;
//		Debug.d(axis + " axis - setting notification off.");
//		if (haveSetNotification) {
//			communicator.sendMessage(snap, new OutgoingAddressMessage(MSG_SetNotification, snap.getAddress().getNullAddress()));
//			haveSetNotification = false;
//		}
//	}
//
//	/**
//	 * 
//	 * @param maxTorque An integer value 0 to 100 representing the maximum torque percentage
//	 * @throws IOException
//	 */
//	public void setMaxTorque(int maxTorque) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent axis drive for " + axis);
//			return;
//		}
//		initialiseIfNeeded();
//		waitTillNotBusy();
//		if (maxTorque > 100) maxTorque = 100;
//		double power = maxTorque * 68.0 / 100.0;
//		byte scaledPower = (byte)power;
//		communicator.lock();
//		Debug.d(axis + " axis - setting maximum torque to: " + maxTorque);
//		try {
//			communicator.sendMessage(snap,
//					new OutgoingByteMessage(MSG_SetPower, scaledPower));
//		}
//		finally {
//			communicator.unlock();
//		}
//		
//	}
//	
//	
//	/**
//	 *
//	 */
//	protected class RequestPositionResponse extends IncomingIntMessage {
//		
//		/**
//		 * @param incomingContext
//		 * @throws IOException
//		 */
//		public RequestPositionResponse(IncomingContext incomingContext) throws IOException {
//			super(incomingContext);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.messages.IncomingIntMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_GetPosition; 
//		}
//	}
//	
//	/**
//	 *
//	 */
//	protected class RequestStatusResponse extends IncomingIntMessage {
//		
//		/**
//		 * @param incomingContext
//		 * @throws IOException
//		 */
//		public RequestStatusResponse(IncomingContext incomingContext) throws IOException {
//			super(incomingContext);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.messages.IncomingIntMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_GetStatus; 
//		}
//	}
//
//	/**
//	 *
//	 */
//	protected class RequestSeekResponse extends IncomingIntMessage {
//		
//		/**
//		 * @param device
//		 * @param message
//		 * @param timeout
//		 * @throws IOException
//		 */
//		public RequestSeekResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
//			super(device, message, timeout);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.messages.IncomingIntMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_Seek; 
//		}
//	}
//	
//	/**
//	 *
//	 */
//	protected class RequestSetPosition extends OutgoingIntMessage {
//		/**
//		 * @param position
//		 */
//		public RequestSetPosition(int position) {
//			super(MSG_SetPosition, position);
//		}
//	}
//
//	protected class RequestSetSpeed extends OutgoingMessage {
//
//		/**
//		 * 
//		 */
//		byte [] message;
//		
//		/**
//		 * The empty constructor will create a message to idle the motor
//		 */
//		RequestSetSpeed() {
//			message = new byte [] { MSG_SetIdle }; 
//		}
//		
//		/**
//		 * Create a message for setting the motor speed.
//		 * @param speed The speed to set the motor to.  Note that specifying
//		 * 0 will stop the motor and hold it at 0 and thus still draws
//		 * high current.  To idle the motor, use the message created by the
//		 * empty constructor.
//		 */
//		RequestSetSpeed(int speed) {
//			byte command;
//			if (speed >= 0) {
//				command = MSG_SetForward;
//			} else {
//				command = MSG_SetReverse;
//				speed = -speed;
//			}
//			if (speed > 255) speed = 255;
//			message = new byte[] { command, (byte)speed };
//				
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.OutgoingMessage#getBinary()
//		 */
//		public byte[] getBinary() {
//			return message;
//		}
//		
//	}
//	
//	/**
//	 *
//	 */
//	protected class RequestOneStep extends OutgoingMessage
//	{
//		/**
//		 * 
//		 */
//		byte [] message;
//		
//		/**
//		 * @param forward
//		 */
//		RequestOneStep(boolean forward) {
//			byte command;
//			if (forward)
//				command = MSG_StepForward;
//			else
//				command = MSG_StepBackward;
//			message = new byte[] { command };
//				
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.OutgoingMessage#getBinary()
//		 */
//		public byte[] getBinary() {
//			return message;
//		}
//	}
//	
//	/**
//	 *
//	 */
//	protected class RequestSeekPosition extends OutgoingMessage {
//		/**
//		 * 
//		 */
//		byte [] message;
//		
//		/**
//		 * @param speed
//		 * @param position
//		 */
//		RequestSeekPosition(int speed, int position) {
//			message = new byte[] { MSG_Seek,
//					(byte)speed,
//					(byte)(position & 0xff),
//					(byte)((position >> 8) & 0xff)};
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.OutgoingMessage#getBinary()
//		 */
//		public byte[] getBinary() {
//			return message;
//		}
//		
//	}
//
//	/**
//	 *
//	 */
//	protected class RequestDDAMaster extends OutgoingMessage {
//		byte [] message;
//		
//		/**
//		 * @param speed
//		 * @param x1
//		 * @param deltaY
//		 */
//		RequestDDAMaster(int speed, int x1, int deltaY) {
//			message = new byte[] { MSG_DDAMaster,
//					(byte)speed,
//					(byte)(x1 & 0xff),
//					(byte)((x1 >> 8) & 0xff),
//					(byte)(deltaY & 0xff),
//					(byte)((deltaY >> 8) & 0xff)
//				};
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.OutgoingMessage#getBinary()
//		 */
//		public byte[] getBinary() {
//			return message;
//		}
//		
//	}
//
//	/**
//	 *
//	 */
//	protected class RequestDDAMasterResponse extends IncomingIntMessage {
//		
//		/**
//		 * @param device
//		 * @param message
//		 * @param timeout
//		 * @throws IOException
//		 */
//		public RequestDDAMasterResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
//			super(device, message, timeout);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.messages.IncomingIntMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_DDAMaster;
//		}
//	}
//	
//	
//	/**
//	 *
//	 */
//	protected class RequestQueue extends OutgoingMessage {
//		byte [] message;
//		
//		/**
//		 * @param speed
//		 * @param x1
//		 * @param deltaY
//		 */
//		RequestQueue(int endX, int endY, int movementSpeed, int control) {
//			message = new byte[] { MSG_QueuePoint,
//					(byte)movementSpeed,
//					(byte)control,
//					(byte)(endX & 0xff),
//					(byte)((endX >> 8) & 0xff),
//					(byte)(endY & 0xff),
//					(byte)((endY >> 8) & 0xff)
//				};
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.OutgoingMessage#getBinary()
//		 */
//		public byte[] getBinary() {
//			return message;
//		}
//		
//	}
//
//	/**
//	 *
//	 */
//	protected class RequestQueueResponse extends IncomingIntMessage {
//		
//		/**
//		 * @param device
//		 * @param message
//		 * @param timeout
//		 * @throws IOException
//		 */
//		public RequestQueueResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
//			super(device, message, timeout);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.messages.IncomingIntMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_QueuePoint;
//		}
//	}
//	
//	
//	
//	
//	/**
//	 *
//	 */
//	protected class RequestRangeResponse extends IncomingIntMessage {
//		
//		/**
//		 * @param incomingContext
//		 * @throws IOException
//		 */
//		public RequestRangeResponse(IncomingContext incomingContext) throws IOException {
//			super(incomingContext);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.messages.IncomingIntMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			// We could get this either as an asynchronous notification
//			// from calibration or by explicit request
//			return packetType == MSG_GetRange || packetType == MSG_Calibrate; 
//		}
//		
//		/**
//		 * @return
//		 * @throws InvalidPayloadException
//		 */
//		public AxisMotor.Range getRange() throws InvalidPayloadException {
//		    byte [] reply = getPayload();
//		    if (reply == null || reply.length != 3)
//		    	throw new InvalidPayloadException("Unexpected payload getting range");
//		    Range r = new AxisMotor.Range();
//		    r.minimum = 0;
//		    r.maximum = IncomingIntMessage.ConvertBytesToInt(reply[1], reply[2]);
//		    return r;
//		}
//
//	}
//	
//	/**
//	 *
//	 */
//	protected class RequestHomeResetResponse extends IncomingMessage {
//		
//		/**
//		 * @param device
//		 * @param message
//		 * @param timeout
//		 * @throws IOException
//		 */
//		public RequestHomeResetResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
//			super(device, message, timeout);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.IncomingMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_HomeReset; 
//		}
//	}
//	
//
//
//}
