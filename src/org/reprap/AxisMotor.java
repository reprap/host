package org.reprap;

import java.io.IOException;
import org.reprap.ReprapException;
import org.reprap.comms.IncomingMessage.InvalidPayloadException;

public interface AxisMotor {


	/**
	 * Dispose of this object
	 */
	public void dispose(); 
	
	
	/**
	 * 
	 *
	 */
	public void waitTillNotBusy() throws IOException;
	
	/**
	 * Reload the preferences
	 *
	 */
	public void refreshPreferences();

	
	/**
	 * Add an XY point to the firmware buffer for plotting
	 * Only works for recent firmware.
	 * 
	 * @param endX
	 * @param endY
	 * @param movementSpeed
	 * @return
	 */
	public boolean queuePoint(int endX, int endY, int movementSpeed, int control) throws IOException; 

	
	/**
	 * Set the motor speed (or turn it off) 
	 * @param speed A value between -255 and 255.
	 * @throws ReprapException
	 * @throws IOException
	 */
	public void setSpeed(int speed) throws IOException; 

	/**
	 * @throws IOException
	 */
	public void setIdle() throws IOException; 
	
	/**
	 * @throws IOException
	 */
	public void stepForward() throws IOException; 
	
	/**
	 * @throws IOException
	 */
	public void stepBackward() throws IOException; 
	
	/**
	 * @throws IOException
	 */
	public void resetPosition() throws IOException; 
	
	/**
	 * @param position
	 * @throws IOException
	 */
	public void setPosition(int position) throws IOException;
	
	/**
	 * Is the comms working?
	 * @return
	 */
	public boolean isAvailable();
	public boolean wasAvailable();
	
	/**
	 * @return current position of the motor
	 * @throws IOException
	 */
	public int getPosition() throws IOException; 
	
	/**
	 * @param speed
	 * @param position
	 * @throws IOException
	 */
	public void seek(int speed, int position) throws IOException; 
	/**
	 * @param speed
	 * @param position
	 * @throws IOException
	 */
	public void seekBlocking(int speed, int position) throws IOException; 

	/**
	 * @param speed
	 * @return range of the motor
	 * @throws IOException
	 * @throws InvalidPayloadException
	 */
	public Range getRange(int speed) throws IOException, InvalidPayloadException; 
	
	/**
	 * @param speed
	 * @throws IOException
	 * @throws InvalidPayloadException
	 */
	public void homeReset(int speed) throws IOException, InvalidPayloadException; 
	
	/**
	 * @param syncType
	 * @throws IOException
	 */
	public void setSync(byte syncType) throws IOException; 
	
	/**
	 * @param speed
	 * @param x1
	 * @param deltaY
	 * @throws IOException
	 */
	public void dda(int speed, int x1, int deltaY) throws IOException; 

	/**
	 * 
	 * @param maxTorque An integer value 0 to 100 representing the maximum torque percentage
	 * @throws IOException
	 */
	public void setMaxTorque(int maxTorque) throws IOException; 
	
	public class Range {
		
		/**
		 * 
		 */
		public int minimum;
		
		/**
		 * 
		 */
		public int maximum;
		
		public Range()
		{
			minimum = 0;
			maximum = 0;
		}
	}

}
