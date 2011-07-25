//package org.reprap.machines;
//
//import java.io.IOException;
//import java.lang.Math;
//
//import org.reprap.Preferences;
//import org.reprap.ReprapException;
//import org.reprap.comms.snap.SNAPAddress;
//import org.reprap.devices.NullExtruder;
//import org.reprap.devices.SNAPExtruder;
//import org.reprap.devices.SNAPStepperMotor;
//import org.reprap.devices.pseudo.LinePrinter;
//import org.reprap.gui.CalibrateZAxis;
//import org.reprap.Extruder;
//import org.reprap.utilities.Debug;
//
///**
// * 
// * A Reprap printer is a 3-D cartesian printer with one or more
// * extruders
// *
// */
//public class SNAPReprap extends GenericRepRap
//{
//	
//	/**
//	 * 
//	 */
//	//private Communicator communicator = org.reprap.Main.getCommunicator();
//	
//	/**
//	* our line printer object.
//	*/
//	private LinePrinter layerPrinter;
//	
//	/**
//	 * @param prefs
//	 * @throws Exception
//	 */
//	public SNAPReprap() throws Exception
//	{
//		super();
//		
//		layerPrinter = new LinePrinter(motorX, motorY, extruders[extruder]);
//		
//		try {			
//			currentX = convertToPositionZ(motorX.getPosition());
//			currentY = convertToPositionZ(motorY.getPosition());
//		} catch (Exception ex) {
//			throw new Exception("Warning: X and/or Y controller not responding, cannot continue");
//		}
//		try {
//			currentZ = convertToPositionZ(motorZ.getPosition());
//		} catch (Exception ex) {
//			System.err.println("Z axis not responding and will be ignored");
//			excludeZ = true;
//		}
//
//	}
//	
//	public void loadMotors()
//	{
//		try
//		{
//			motorX = new SNAPStepperMotor(org.reprap.Main.getCommunicator(),
//					new SNAPAddress(Preferences.loadGlobalInt("XAxisAddress")), 1);
//			motorY = new SNAPStepperMotor(org.reprap.Main.getCommunicator(),
//					new SNAPAddress(Preferences.loadGlobalInt("YAxisAddress")), 2);
//			motorZ = new SNAPStepperMotor(org.reprap.Main.getCommunicator(),
//					new SNAPAddress(Preferences.loadGlobalInt("ZAxisAddress")), 3);
//
//			motorX.setPrinter(this);
//			motorY.setPrinter(this);
//			motorZ.setPrinter(this);	
//		} catch (Exception ex){
//			ex.printStackTrace();
//		}
//	}
//	
//	public void loadExtruders()
//	{
//		extruders = new SNAPExtruder[extruderCount];
//		
//		super.loadExtruders();
//	}
//	
//	public Extruder extruderFactory(int count)
//	{
//		try
//		{
//			String prefix = "Extruder" + count + "_";
//			SNAPAddress addy = new SNAPAddress(Preferences.loadGlobalInt(prefix + "Address"));
//			return new SNAPExtruder(org.reprap.Main.getCommunicator(), addy, count, this);
//		}
//		catch (Exception e)
//		{
//			return new NullExtruder(count, this);
//		}
//	}
//	
//	public void refreshPreferences()
//	{
//		super.refreshPreferences();
//		
//		motorX.refreshPreferences();
//		motorY.refreshPreferences();
//		motorZ.refreshPreferences();
//	}
//	
//	public void startRun()
//	{
//		
//	}
//	
//	/**
//	 * Wait while the motors move about
//	 * @throws IOException
//	 */
//	public void waitTillNotBusy() throws IOException
//	{
//		motorX.waitTillNotBusy();
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Printer#calibrate()
//	 */
//	public void calibrate()
//	{
//	}
//	
//	/**
//	 * Go to the purge point
//	 */
//	public void moveToPurge()
//	{
//		singleMove(dumpX, dumpY, currentZ, getExtruder().getFastXYFeedrate());
//	}
//
//	/* (non-Javadoc)
//	 * @see org.reprap.Printer#moveTo(double, double, double, boolean, boolean)
//	 */
//	public void moveTo(double x, double y, double z, double feedrate, boolean startUp, boolean endUp) throws ReprapException, IOException {
//		
//		if (isCancelled()) return;
//		
//		currentFeedrate = feedrate;
//		
//		int stepperX = convertToStepX(x);
//		int stepperY = convertToStepY(y);
//		int stepperZ = convertToStepZ(z);
//		int currentStepperX = convertToStepX(currentX);
//		int currentStepperY = convertToStepY(currentY);
//		int currentStepperZ = convertToStepZ(currentZ);		
//		
//		if (currentStepperX == stepperX && 
//				currentStepperY ==stepperY && 
//				currentStepperZ == stepperZ && 
//				!startUp)
//			return;
//
//		// We don't need to lift a whole layer up. Half a layer should do
//		// and will dribble less. Remember the Z axis is kinda slow...
//		double liftedZ = z + (extruders[extruder].getMinLiftedZ());
//		int stepperLiftedZ = convertToStepZ(liftedZ);
//		int targetZ;
//		
//		// Raise head slightly before move?
//		if(startUp)
//		{
//			targetZ = stepperLiftedZ;
//			currentZ = liftedZ;
//		} else
//		{
//			targetZ = stepperZ;
//			currentZ = z;
//		}
//		
//		if (targetZ != currentStepperZ) {
//			totalDistanceMoved += Math.abs(currentZ - liftedZ);
//			int zSpeed = convertFeedrateToSpeedZ(getFastFeedrateZ());
//			if (!excludeZ) motorZ.seekBlocking(zSpeed, targetZ);
//			if (idleZ) motorZ.setIdle();
//			currentStepperZ = targetZ;
//		}
//		
//		int currentSpeedXY = convertFeedrateToSpeedXY(getFeedrate());
//		layerPrinter.moveTo(stepperX, stepperY, currentSpeedXY, false, false);
//		totalDistanceMoved += segmentLength(x - currentX, y - currentY);
//		currentX = x;
//		currentY = y;
//		
//		if(endUp)
//		{
//			targetZ = stepperLiftedZ;
//			currentZ = liftedZ;
//		} else
//		{
//			targetZ = stepperZ;
//			currentZ = z;
//		}
//		
//		// Move head back down to surface?
//		if(targetZ != currentStepperZ)
//		{
//			totalDistanceMoved += Math.abs(currentZ - z);
//			int zSpeed = convertFeedrateToSpeedZ(maxFeedrateZ);
//			if (!excludeZ) motorZ.seekBlocking(zSpeed, targetZ);
//			if (idleZ) motorZ.setIdle();
//			currentStepperZ = targetZ;
//		} 
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Printer#printTo(double, double, double, boolean)
//	 */
//	public void printTo(double x, double y, double z, double feedrate, boolean stopExtruder, boolean closeValve) 
//		throws ReprapException, IOException 
//	{
//		if (isCancelled()) return;
//		EnsureNotEmpty();
//		if (isCancelled()) return;
//		EnsureHot();
//		if (isCancelled()) return;
//		
//		currentFeedrate = feedrate;
//		
//		maybeReZero();
//
//		int stepperX = convertToStepX(x);
//		int stepperY = convertToStepY(y);
//		int stepperZ = convertToStepZ(z);
//		
//		if ((stepperX != layerPrinter.getCurrentX() || stepperY != layerPrinter.getCurrentY()) && z != currentZ)
//			throw new ReprapException("Reprap cannot print a line across 3 axes simultaneously");
//
////		if (previewer != null)
////			previewer.addSegment(convertToPositionX(layerPrinter.getCurrentX()),
////					convertToPositionY(layerPrinter.getCurrentY()), currentZ,
////					x, y, z);
//
//		if (isCancelled()) return;
//		
//		
//		if (z != currentZ) 
//		{
//			Debug.d("Printing a vertical extrusion.  Should we do that?");
//			// Print a simple vertical extrusion
//			double distance = Math.abs(currentZ - z);
//			totalDistanceExtruded += distance;
//			totalDistanceMoved += distance;
//			extruders[extruder].setMotor(true);
//			int zSpeed = convertFeedrateToSpeedZ(maxFeedrateZ);
//			if (!excludeZ) motorZ.seekBlocking(zSpeed, stepperZ);
//			extruders[extruder].setMotor(false);
//			currentZ = z;
//			return;
//		}
//		
//
//
//		// Otherwise printing only in X/Y plane
//		double deltaX = x - currentX;
//		double deltaY = y - currentY;
//		double distance = segmentLength(deltaX, deltaY);
//		totalDistanceExtruded += distance;
//		totalDistanceMoved += distance;
//		if (segmentPauseCheckbox != null && distance > 0)
//			if(segmentPauseCheckbox.isSelected())
//				segmentPause();
//
//		int currentSpeedXY = convertFeedrateToSpeedXY(getExtruder().getFastXYFeedrate());
//		//System.out.println("close: " + closeValve + ", stop ex:" + stopExtruder);
//		layerPrinter.printTo(stepperX, stepperY, currentSpeedXY, getExtruder().getExtruderSpeed(), stopExtruder, closeValve);
//		currentX = x;
//		currentY = y;
//	}
//	
//	public void stopMotor() throws IOException
//	{
//		layerPrinter.stopMotor();
//	}
//	
//	public void stopValve() throws IOException
//	{
//		layerPrinter.stopValve();
//	}
//
//	/* Move to zero stop on X axis.
//	 * (non-Javadoc)
//	 * @see org.reprap.Printer#homeToZeroX() 
//	 */
//	public void homeToZeroX() throws ReprapException, IOException {
//		
//		int fastSpeedXY = convertFeedrateToSpeedXY(getExtruder().getFastXYFeedrate());
//		motorX.homeReset(fastSpeedXY);
//		layerPrinter.zeroX();
//
//		super.homeToZeroX();
//	}
//	
//	/* Move to zero stop on Y axis.
//	 * (non-Javadoc)
//	 * @see org.reprap.Printer#homeToZeroY()
//	 */
//	public void homeToZeroY() throws ReprapException, IOException {
//		
//		int fastSpeedXY = convertFeedrateToSpeedXY(getExtruder().getFastXYFeedrate());
//		motorY.homeReset(fastSpeedXY);
//		layerPrinter.zeroY();
//
//		super.homeToZeroX();
//	}
//	
//	/* Move to zero stop on Z axis.
//	 * (non-Javadoc)
//	 * @see org.reprap.Printer#homeToZeroZ()
//	 */
//	public void homeToZeroZ() throws ReprapException, IOException {
//		
//		int fastSpeedZ = convertFeedrateToSpeedZ(getFastFeedrateZ());
//		motorZ.homeReset(fastSpeedZ);
//
//		super.homeToZeroZ();
//	}
//	
//	public void home()
//	{
//		try
//		{
//			int fastSpeedXY = convertFeedrateToSpeedXY(getExtruder().getFastXYFeedrate());
//			int fastSpeedZ = convertFeedrateToSpeedZ(getFastFeedrateZ());
//
//			motorX.homeReset(fastSpeedXY);
//			motorY.homeReset(fastSpeedXY);
//			if (!excludeZ) motorZ.homeReset(fastSpeedZ);
//		}
//		catch (Exception e)
//		{
//			System.err.println("Error homing all axes.");
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see org.reprap.Printer#terminate()
//	 */
//	public void terminate() throws Exception {
//		motorX.setIdle();
//		motorY.setIdle();
//		if (!excludeZ) motorZ.setIdle();
//		
//		super.terminate();
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Printer#dispose()
//	 */
//	public void dispose() {
//		motorX.dispose();
//		motorY.dispose();
//		motorZ.dispose();
//		
//		super.dispose();
//
////		communicator.close();
////		communicator.dispose();
//	}
//	
//	/**
//	 * 
//	 */
//	private void EnsureNotEmpty() {
//		if (!extruders[extruder].isEmpty()) return;
//		
//		while (extruders[extruder].isEmpty() && !isCancelled()) {
//			//if (previewer != null)
//				//previewer.
//				setMessage("Extruder is out of feedstock.  Waiting for refill.");
//				machineWait(1000);
//		}
//		//if (previewer != null) previewer.
//		setMessage(null);
//	}
//	
//	/**
//	 * @throws ReprapException
//	 * @throws IOException
//	 */
//	private void EnsureHot() throws ReprapException, IOException {
//		if(extruders[extruder].getTemperatureTarget() <= Preferences.absoluteZero() + 1)
//			return;
//		
//		double threshold = extruders[extruder].getTemperatureTarget() * 0.90;	// Changed from 0.95 by Vik.
//		
//		if (extruders[extruder].getTemperature() >= threshold)
//			return;
//		
//
//		double x = currentX;
//		double y = currentY;
//		int tempReminder=0;
//		temperatureReminder();
//		Debug.d("Moving to heating zone");
//		double oldFeedrate = getFeedrate();
//		
//		// Ensure the extruder is off
//		
//		extruders[extruder].setMotor(false);
//				
//		moveToHeatingZone();
//		while(extruders[extruder].getTemperature() < threshold && !isCancelled()) {
//			//if (previewer != null) previewer.
//			setMessage("Waiting for extruder to reach working temperature (" + 
//					Math.round(extruders[extruder].getTemperature()) + ")");
//				machineWait(1000);
//				// If it stays cold for 10s, remind it of its purpose.
//				if (tempReminder++ >10) {
//					tempReminder=0;
//					temperatureReminder();
//				}
//		}
//		Debug.d("Returning to previous position");
//		moveTo(x, y, currentZ, currentFeedrate, true, false);
//		
//		currentFeedrate = oldFeedrate;
//		
//		//if (previewer != null) previewer.
//		setMessage(null);
//		
//	}
//
//	/** A bodge to fix the extruder's current tendency to forget what temperature
//	 * it is supposed to be reaching.
//	 * 
//	 * Vik
//	 */
//	private void temperatureReminder() {
//		if(extruders[extruder].getTemperatureTarget() < Preferences.absoluteZero())
//			return;
//		Debug.d("Reminding it of the temperature");
//		try {
//			extruders[extruder].setTemperature(extruders[extruder].getTemperatureTarget(), false);
//			//setTemperature(Preferences.loadGlobalInt("ExtrusionTemp"));
//		} catch (Exception e) {
//			System.err.println("Error resetting temperature.");
//		}
//	}
//	
//	/**
//	 * Moves the head to the predefined heating area
//	 * @throws IOException
//	 * @throws ReprapException
//	 */
//	private void moveToHeatingZone() throws ReprapException, IOException {
//		//setFeedrate(getFastFeedrateXY());
//		moveTo(1, 1, currentZ, getExtruder().getFastXYFeedrate(), true, false);
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Printer#setZManual(double)
//	 */
//	public void setZManual(double zeroPoint) throws IOException {
//		
//		int zSpeed = convertFeedrateToSpeedZ(getFastFeedrateZ());
//		CalibrateZAxis msg =
//			new CalibrateZAxis(null, motorZ, scaleZ, zSpeed);
//		msg.setVisible(true);
//		try {
//			synchronized(msg) {
//				msg.wait();
//			}
//		} catch (Exception ex) {
//		}
//		msg.dispose();
//		
//		motorZ.setPosition(convertToStepZ(zeroPoint));
//	}
//	
//	/**
//	 * All machine dwells and delays are routed via this function, rather than 
//	 * calling Thread.sleep - this allows them to generate the right G codes (G4) etc.
//	 * 
//	 * The RS232/USB etc comms system doesn't use this - it sets its own delays.
//	 * @param milliseconds
//	 */
//	public void machineWait(double milliseconds)
//	{
//		if(milliseconds <= 0)
//			return;
//		try {
//			Thread.sleep((long)milliseconds);
//		} catch (InterruptedException e) {
//		}		
//	}
//	
//	public void waitWhileBufferNotEmpty()
//	{
//	}
//	
//	public void slowBuffer()
//	{
//	}
//	
//	public void speedBuffer()
//	{
//	}
//	
//	/**
//	 * Load a GCode file to be made.
//	 * @return the name of the file
//	 */
//	public String loadGCodeFileForMaking()
//	{
//		System.err.println("SNAP RepRap: attempt to load GCode file.");
//		//super.loadGCodeFileForMaking();
//		return null;
//	}
//	
//	/**
//	 * Set an output file
//	 * @return
//	 */
//	public String setGCodeFileForOutput(String fileRoot)
//	{
//		System.err.println("SNAP RepRap: cannot generate GCode file.");
//		return null;		
//	}
//
//	/**
//	 * If a file replay is being done, do it and return true
//	 * otherwise return false.
//	 * @return
//	 */
//	public boolean filePlay()
//	{
//		return false;
//	}
//}
//
//
