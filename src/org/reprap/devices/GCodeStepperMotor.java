//package org.reprap.devices;
//
//import java.io.IOException;
//import org.reprap.utilities.Debug;
//import org.reprap.AxisMotor;
//import org.reprap.ReprapException;
//import org.reprap.comms.IncomingMessage.InvalidPayloadException;
//import org.reprap.devices.GenericStepperMotor;
//import org.reprap.machines.GCodeRepRap;
//
//public class GCodeStepperMotor extends GenericStepperMotor {
//	
//	GCodeRepRap printer;
//	/**
//	 * @param motorId
//	 */
//	public GCodeStepperMotor(GCodeRepRap p, int motorId) {
//		super(null, motorId);
//		printer = p;
//	}
//
//	/**
//	 * Is the comms working?
//	 * @return
//	 */
//	public boolean isAvailable()
//	{
//		return true;
//	}
//	
//	public boolean wasAvailable()
//	{
//		return true;
//	}
//	public void refreshPreferences()
//	{
//	}
//
//
//	
//	/**
//	 * Dispose of this object
//	 */
//	public void dispose() {
//	}
//	
//	
//	/**
//	 * 
//	 *
//	 */
//	public void waitTillNotBusy() throws IOException
//	{
//		return;	
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
//		return false;
//	}
//	
//	/**
//	 * Set the motor speed (or turn it off) 
//	 * @param speed A value between -255 and 255.
//	 * @throws ReprapException
//	 * @throws IOException
//	 */
//	public void setSpeed(int speed) throws IOException {
//		Debug.d("GCodeStepperMotor! " + axis + " axis - setting speed: " + speed);
//	}
//
//	/**
//	 * @throws IOException
//	 */
//	public void setIdle() throws IOException {
//		Debug.d("GCodeStepperMotor! " + axis + " axis - going idle.");
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	public void stepForward() throws IOException {
//		double x, y, z;
//		x = printer.getX();
//		y = printer.getY();
//		z = printer.getZ();
//		try
//		{
//			switch(mid)
//			{
//			case 1:
//				printer.moveTo(x + 1.0/printer.getXStepsPerMM(), y, z, printer.getExtruder().getSlowXYFeedrate(), false, false);
//				break;
//			case 2:
//				printer.moveTo(x, y + 1.0/printer.getYStepsPerMM(), z, printer.getExtruder().getSlowXYFeedrate(), false, false);
//				break;
//			case 3:
//				printer.moveTo(x, y, z + 1.0/printer.getZStepsPerMM(), printer.getFastFeedrateZ(), false, false);
//				break;
//			default:
//				System.err.println("GCodeStepperMotor - stepForward.  Dud motor id: " + mid);
//			}
//		} catch (Exception ex)
//		{}
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	public void stepBackward() throws IOException {
//		double x, y, z;
//		x = printer.getX();
//		y = printer.getY();
//		z = printer.getZ();
//		try
//		{
//			switch(mid)
//			{
//			case 1:
//				printer.moveTo(x - 1.0/printer.getXStepsPerMM(), y, z, printer.getExtruder().getSlowXYFeedrate(), false, false);
//				break;
//			case 2:
//				printer.moveTo(x, y - 1.0/printer.getYStepsPerMM(), z, printer.getExtruder().getSlowXYFeedrate(), false, false);
//				break;
//			case 3:
//				printer.moveTo(x, y, z - 1.0/printer.getZStepsPerMM(), printer.getFastFeedrateZ(), false, false);
//				break;
//			default:
//				System.err.println("GCodeStepperMotor - stepBackward.  Dud motor id: " + mid);
//			}
//		} catch (Exception ex)
//		{}
//	}
//	
//	/**
//	 * @throws IOException
//	 */
//	public void resetPosition() throws IOException {
//		setPosition(0);
//	}
//	
//	/**
//	 * @param position
//	 * @throws IOException
//	 */
//	public void setPosition(int position) throws IOException {
//		Debug.d("GCodeStepperMotor! " + axis + " axis - setting position to: " + position);	
//	}
//	
//	/**
//	 * @return current position of the motor
//	 * @throws IOException
//	 */
//	public int getPosition() throws IOException {
//		switch(mid)
//		{
//		case 1:
//			return (int)Math.round(printer.getX()*printer.getXStepsPerMM());
//			
//		case 2:
//			return (int)Math.round(printer.getY()*printer.getYStepsPerMM());
//			
//		case 3:
//			return (int)Math.round(printer.getZ()*printer.getZStepsPerMM());
//			
//		default:
//			System.err.println("GCodeStepperMotor - getPosition.  Dud motor id: " + mid);
//		}
//		return 0;
//	}
//	
//	/**
//	 * @param speed
//	 * @param position
//	 * @throws IOException
//	 */
//	public void seek(int speed, int position) throws IOException {
//		double x, y, z;
//		x = printer.getX();
//		y = printer.getY();
//		z = printer.getZ();
//		try
//		{
//			switch(mid)
//			{
//			case 1:
//				//printer.setFeedrate(printer.getFastFeedrateXY());
//				x = (double)position/printer.getXStepsPerMM();
//				break;
//			case 2:
//				//printer.setFeedrate(printer.getFastFeedrateXY());
//				y = (double)position/printer.getYStepsPerMM();
//				break;
//			case 3:
//				//printer.setFeedrate(printer.getFastFeedrateZ());
//				z = (double)position/printer.getZStepsPerMM();
//				break;
//			default:
//				System.err.println("GCodeStepperMotor - seek.  Dud motor id: " + mid);
//			}
//			printer.moveTo(x, y, z, printer.getCurrentFeedrate(), false, false);
//		} catch (Exception ex)
//		{}
//	}
//
//	/**
//	 * @param speed
//	 * @param position
//	 * @throws IOException
//	 */
//	public void seekBlocking(int speed, int position) throws IOException {
//		seek(speed, position);
//	}
//
//	/**
//	 * @param speed
//	 * @return range of the motor
//	 * @throws IOException
//	 * @throws InvalidPayloadException
//	 */
//	public AxisMotor.Range getRange(int speed) throws IOException, InvalidPayloadException {
//		Debug.d(axis + " axis - getting range.");
//		return new AxisMotor.Range();
//	}
//	
//	/**
//	 * @param speed
//	 * @throws IOException
//	 * @throws InvalidPayloadException
//	 */
//	public void homeReset(int speed) throws IOException, InvalidPayloadException {
//		try
//		{
//			switch(mid)
//			{
//			case 1:
//				printer.homeToZeroX();
//				break;
//			case 2:
//				printer.homeToZeroY();
//				break;
//			case 3:
//				printer.homeToZeroZ();
//				break;
//			default:
//				System.err.println("GCodeStepperMotor - homeReset.  Dud motor id: " + mid);
//			}
//		} catch (Exception ex)
//		{}
//	}
//	
//	/**
//	 * @param syncType
//	 * @throws IOException
//	 */
//	public void setSync(byte syncType) throws IOException {
//		Debug.d("GCodeStepperMotor! " + axis + " axis - setting sync to " + syncType);
//	}
//	
//	/**
//	 * @param speed
//	 * @param x1
//	 * @param deltaY
//	 * @throws IOException
//	 */
//	public void dda(int speed, int x1, int deltaY) throws IOException {
//		Debug.d("GCodeStepperMotor! " + axis + " axis - dda at speed " + speed + ". x1 = " + x1 + ", deltaY = " + deltaY);
//	}
//	
//
//	/**
//	 * 
//	 * @param maxTorque An integer value 0 to 100 representing the maximum torque percentage
//	 * @throws IOException
//	 */
//	public void setMaxTorque(int maxTorque) throws IOException {
//		Debug.d("GCodeStepperMotor! " + axis + " axis - setting maximum torque to: " + maxTorque);
//	}
//	
//}
