//package org.reprap.devices;
//
//import org.reprap.Device;
//import org.reprap.Printer;
//import org.reprap.AxisMotor;
//import org.reprap.comms.Address;
//
///**
// *
// */
//public abstract class GenericStepperMotor extends Device implements AxisMotor {
//	
//
//	/**
//	 * 
//	 */
//	protected boolean haveInitialised = false;
//	protected boolean haveSetNotification = false;
//	protected boolean haveCalibrated = false;
//	
//	
//	/**
//	 * Useful to know what we're called
//	 */
//	protected String axis;
//	protected int mid;
//	
//	/**
//	 * To whom (grammar) do I belong?
//	 * if null, device is a brain in a bottle 
//	 * (i.e. just working alone on the bench).
//	 */
//	public Printer printer = null;
//	
//
//	/**
//	 * @param communicator
//	 * @param address
//	 * @param prefs
//	 * @param motorId
//	 */
//	public GenericStepperMotor(Address address, int motorId) {
//		super(address);
//		mid = motorId;	
//		switch(motorId)
//		{
//		case 1:
//			axis = "X";
//			break;
//		case 2:
//			axis = "Y";
//			break;
//		case 3:
//			axis = "Z";
//			break;
//		default:
//			axis = "X";
//			System.err.println("GenericStepperMotor - dud axis id: " + motorId);
//			Thread.dumpStack();	
//		}
//		
//		refreshPreferences();
//
//	}
//	
////	public boolean isAvailable()
////	{
////		return false;
////	}
//	
////	public void refreshPreferences()
////	{
////	}
////
////	/**
////	 * @throws IOException
////	 */
////	private void initialiseIfNeeded() throws IOException {
////			haveInitialised = true;
////	}
////	
////	/**
////	 * Dispose of this object
////	 */
////	public void dispose() {
////	}
////	
////	
////	private int getStatus() throws IOException
////	{
////		return 0;
////	}
////	
////	/**
////	 * 
////	 *
////	 */
////	private void waitTillQueueNotFull() throws IOException
////	{
////		return;
////	}
////	
////	/**
////	 * 
////	 *
////	 */
////	public void waitTillNotBusy() throws IOException
////	{
////		return;	
////	}	
////	
////	/**
////	 * Add an XY point to the firmware buffer for plotting
////	 * Only works for recent firmware.
////	 * 
////	 * @param endX
////	 * @param endY
////	 * @param movementSpeed
////	 * @return
////	 */
////	public boolean queuePoint(int endX, int endY, int movementSpeed, int control) throws IOException 
////	{
////		return false;
////	}
////	
////	/**
////	 * Set the motor speed (or turn it off) 
////	 * @param speed A value between -255 and 255.
////	 * @throws ReprapException
////	 * @throws IOException
////	 */
////	public void setSpeed(int speed) throws IOException {
////		Debug.d(axis + " axis - setting speed: " + speed);
////	}
////
////	/**
////	 * @throws IOException
////	 */
////	public void setIdle() throws IOException {
////		Debug.d(axis + " axis - going idle.");
////	}
////	
////	/**
////	 * @throws IOException
////	 */
////	public void stepForward() throws IOException {
////		Debug.d(axis + " axis - stepping forward.");		
////	}
////	
////	/**
////	 * @throws IOException
////	 */
////	public void stepBackward() throws IOException {
////		Debug.d(axis + " axis - stepping backward.");
////	}
////	
////	/**
////	 * @throws IOException
////	 */
////	public void resetPosition() throws IOException {
////		setPosition(0);
////	}
////	
////	/**
////	 * @param position
////	 * @throws IOException
////	 */
////	public void setPosition(int position) throws IOException {
////		Debug.d(axis + " axis - setting position to: " + position);	
////	}
////	
////	/**
////	 * @return current position of the motor
////	 * @throws IOException
////	 */
////	public int getPosition() throws IOException {
////		int value = 0;
////		Debug.d(axis + " axis - getting position.  It is... ");
////		Debug.d("..." + value);		
////		return value;
////	}
////	
////	/**
////	 * @param speed
////	 * @param position
////	 * @throws IOException
////	 */
////	public void seek(int speed, int position) throws IOException {
////		Debug.d(axis + " axis - seeking position " + position + " at speed " + speed);
////	}
////
////	/**
////	 * @param speed
////	 * @param position
////	 * @throws IOException
////	 */
////	public void seekBlocking(int speed, int position) throws IOException {
////		Debug.d(axis + " axis - seeking-blocking position " + position + " at speed " + speed);
////	}
////
////	/**
////	 * @param speed
////	 * @return range of the motor
////	 * @throws IOException
////	 * @throws InvalidPayloadException
////	 */
////	public AxisMotor.Range getRange(int speed) throws IOException, InvalidPayloadException {
////		Debug.d(axis + " axis - getting range.");
////		return new AxisMotor.Range();
////	}
////	
////	/**
////	 * @param speed
////	 * @throws IOException
////	 * @throws InvalidPayloadException
////	 */
////	public void homeReset(int speed) throws IOException, InvalidPayloadException {
////		Debug.d(axis + " axis - home reset at speed " + speed);
////	}
////	
////	/**
////	 * @param syncType
////	 * @throws IOException
////	 */
////	public void setSync(byte syncType) throws IOException {
////		Debug.d(axis + " axis - setting sync to " + syncType);
////	}
////	
////	/**
////	 * @param speed
////	 * @param x1
////	 * @param deltaY
////	 * @throws IOException
////	 */
////	public void dda(int speed, int x1, int deltaY) throws IOException {
////		Debug.d(axis + " axis - dda at speed " + speed + ". x1 = " + x1 + ", deltaY = " + deltaY);
////	}
////	
////	/**
////	 * @throws IOException
////	 */
////	private void setNotification() throws IOException {
////		Debug.d(axis + " axis - setting notification on.");
////	}
////
////	/**
////	 * @throws IOException
////	 */
////	private void setNotificationOff() throws IOException {
////		Debug.d(axis + " axis - setting notification off.");
////	}
////
////	/**
////	 * 
////	 * @param maxTorque An integer value 0 to 100 representing the maximum torque percentage
////	 * @throws IOException
////	 */
////	public void setMaxTorque(int maxTorque) throws IOException {
////		Debug.d(axis + " axis - setting maximum torque to: " + maxTorque);
////	}
////	
//	public void setPrinter(Printer p)
//	{
//		printer = p;
//	}
//	
//	public Printer getPrinter()
//	{
//		return printer;
//	}	
//	
//}
