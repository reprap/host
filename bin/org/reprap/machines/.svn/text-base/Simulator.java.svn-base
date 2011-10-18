package org.reprap.machines;

import java.io.IOException;

import org.reprap.Extruder;
//import org.reprap.devices.NullStepperMotor;
import org.reprap.devices.NullExtruder;
import org.reprap.utilities.Debug;

/**
 *
 */
public class Simulator extends GenericRepRap {
	
	/**
	 * @param config
	 */
	public Simulator() throws Exception {
		super();
	}
	
	public void loadMotors()
	{
//		motorX = new NullStepperMotor(1);
//		motorY = new NullStepperMotor(2);
//		motorZ = new NullStepperMotor(3);
	}
	
	public Extruder extruderFactory(int count)
	{
		return new NullExtruder(count, this);
	}
	
	public void startRun()
	{
		
	}
	
	public boolean iAmPaused()
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.Printer#terminate()
	 */
	public void terminate() throws Exception
	{
		//Debug.e("Generic terminate: " + getFinishX() + " " + getFinishY());
		moveTo(getFinishX(), getFinishY(), getZ(), getExtruder().getFastXYFeedrate(), true, true);
		getExtruder().setMotor(false);
		getExtruder().setValve(false);
		getExtruder().setTemperature(0, false);
	}
	
	
	
	public void waitTillNotBusy() throws IOException {}
	public void finishedLayer(int layerNumber) throws Exception {}
	public void betweenLayers(int layerNumber) throws Exception{}
	public void startingLayer(int layerNumber) throws Exception {}

	public void printTo(double x, double y, double z, double feedRate, boolean stopExtruder, boolean closeValve) 
	{
		//if (previewer != null)
		//	previewer.addSegment(currentX, currentY, currentZ, x, y, z);
		if (isCancelled())
			return;

		double distance = segmentLength(x - currentX, y - currentY);
		if (z != currentZ)
			distance += Math.abs(currentZ - z);

		totalDistanceExtruded += distance;
		totalDistanceMoved += distance;
		currentX = x;
		currentY = y;
		currentZ = z;
	}
	
	public double[] getCoordinates() throws Exception
	{
		double [] result = new double[4];
		result[0] = currentX;
		result[1] = currentY;
		result[2] = currentZ;
		result[3] = getExtruder().getExtruderState().length();
		
		return result;
	}
	
	public double[] getZeroError() throws Exception
	{
		double [] result = new double[4];
		result[0] = 0;
		result[1] = 0;
		result[2] = 0;
		result[3] = 0;
		
		return result;
	}
	
	public void delay(long millis) {}
	
	//TODO: make this work normally.
	public void stopValve() throws IOException
	{
	}
	
	//TODO: make this work normally.
	public void stopMotor() throws IOException
	{
	}
	
	/**
	 * All machine dwells and delays are routed via this function, rather than 
	 * calling Thread.sleep - this allows them to generate the right G codes (G4) etc.
	 * 
	 * The RS232/USB etc comms system doesn't use this - it sets its own delays.
	 * 
	 * Here do no delay; it makes no sense for the simulation machine
	 * @param milliseconds
	 */
	public void machineWait(double milliseconds, boolean fastExtrude)
	{
	}
	
	public void waitWhileBufferNotEmpty()
	{
	}
	
	public void slowBuffer()
	{
	}
	
	public void speedBuffer()
	{
	}
	
	/**
	 * Load a GCode file to be made.
	 * @return the name of the file
	 */
	public String loadGCodeFileForMaking()
	{
		Debug.e("Simulator: attempt to load GCode file.");
		//super.loadGCodeFileForMaking();
		return null;
	}
	
	/**
	 * Set an output file
	 * @return
	 */
	public String setGCodeFileForOutput(String fileRoot)
	{
		Debug.e("Simulator: cannot generate GCode file.");
		return null;		
	}
	
	public boolean filePlay()
	{
		return false;
	}
	
	public void stabilise()
	{}
	
	public double getBedTemperature()
	{
		return bedTemperatureTarget;
	}
}
