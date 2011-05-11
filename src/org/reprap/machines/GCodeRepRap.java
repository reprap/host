package org.reprap.machines;

/*
 * TODO: To do's:
 * 
 * TODO: fixup warmup segments GCode (forgets to turn on extruder) 
 * TODO: fixup all the RR: println commands 
 * TODO: find a better place for the code. You cannot even detect a layer change without hacking now. 
 * TODO: read Zach's GCode examples to check if I messed up. 
 * TODO: make GCodeWriter a subclass of NullCartesian, so I don't have to fix code all over the place.
 */

import org.reprap.ReprapException;
import org.reprap.Extruder;
import org.reprap.Preferences;
import org.reprap.comms.GCodeReaderAndWriter;
import org.reprap.utilities.Debug;
import org.reprap.devices.GCodeExtruder;
//import org.reprap.devices.GCodeStepperMotor;
import org.reprap.geometry.LayerRules;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 */
public class GCodeRepRap extends GenericRepRap {
	
	/**
	* our class to send gcode instructions
	*/
	GCodeReaderAndWriter gcode;
	
	/**
	 * @param prefs
	 * @throws Exception
	 */
	public GCodeRepRap() throws Exception {
		
		super();

		gcode = new GCodeReaderAndWriter();
		gcode.queue("M110 ; Reset the line numbers");
		//gcode.queue("M115 ; Get the firmware version numbers etc");
		//String FirmwareConfig = gcode.lastResponse();
		//FirmwareConfig = FirmwareConfig.replace('\\','\n'); //make it easier to read for humans if it has "continuations" 
		//Debug.d("Firmware configuration string: " + FirmwareConfig);
		loadExtruders();
		
		forceSelection = true;
	}
	
	public void loadMotors()
	{
//		motorX = new GCodeStepperMotor(this, 1);
//		motorY = new GCodeStepperMotor(this, 2);
//		motorZ = new GCodeStepperMotor(this, 3);
	}
	
	public void loadExtruders() throws Exception
	{
		try
		{
			int extruderCount = Preferences.loadGlobalInt("NumberOfExtruders");
			extruders = new GCodeExtruder[extruderCount];
		} catch (Exception e)
		{
			Debug.e(e.toString());
		}
		
		super.loadExtruders();
	}
	
	public Extruder extruderFactory(int count)
	{
		return new GCodeExtruder(gcode, count, this);
	}
	
	private void qFeedrate(double feedrate) throws Exception
	{		
		if(currentFeedrate == feedrate)
			return;
		gcode.queue("G1 F" + feedrate + "; feed for start of next move");
		currentFeedrate = feedrate;		
	}
	
	private void qXYMove(double x, double y, double feedrate) throws Exception
	{	
		double dx = x - currentX;
		double dy = y - currentY;
		
		double xyFeedrate = round(extruders[extruder].getFastXYFeedrate(), 1);
		
		if(xyFeedrate < feedrate)
		{
			Debug.d("GCodeRepRap().qXYMove: feedrate (" + feedrate + ") exceeds maximum (" + xyFeedrate + ").");
			feedrate = xyFeedrate;
		}
		
		if(getExtruder().getMaxAcceleration() <= 0)
			qFeedrate(feedrate);
		
		
		if(dx == 0.0 && dy == 0.0)
		{
			if(currentFeedrate != feedrate)
				qFeedrate(feedrate);
			return;
		}
		
		double extrudeLength;
		String code = "G1 ";

		if (dx != 0)
			code += "X" + x;
		if (dy != 0)
			code += " Y" + y;

		extrudeLength = extruders[extruder].getDistance(Math.sqrt(dx*dx + dy*dy));

		if(extrudeLength > 0)
		{
			if(extruders[extruder].getReversing())
				extruders[extruder].getExtruderState().add(-extrudeLength);
			else
				extruders[extruder].getExtruderState().add(extrudeLength);
			if(extruders[extruder].get5D())
				code += " E" + round(extruders[extruder].getExtruderState().length(), 1);
		}
		
		if (currentFeedrate != feedrate)
		{
			code += " F" + feedrate;
			currentFeedrate = feedrate;
		}
		
		code += " ;horizontal move";
		gcode.queue(code);
		currentX = x;
		currentY = y;
	}
	
	private void qZMove(double z, double feedrate) throws Exception
	{	
		// note we set the feedrate whether we move or not
		
		double zFeedrate = round(getMaxFeedrateZ(), 1);
		
		if(zFeedrate < feedrate)
		{
			Debug.d("GCodeRepRap().qZMove: feedrate (" + feedrate + ") exceeds maximum (" + zFeedrate + ").");
			feedrate = zFeedrate;
		}
		
		if(getMaxZAcceleration() <= 0)
			qFeedrate(feedrate);
		
		double dz = z - currentZ;
		
		if(dz == 0.0)
			return;
		
		String code;
		double extrudeLength;
		
		code = "G1 Z" + z;

		extrudeLength = extruders[extruder].getDistance(dz);

		if(extrudeLength > 0)
		{
			if(extruders[extruder].getReversing())
				extruders[extruder].getExtruderState().add(-extrudeLength);
			else
				extruders[extruder].getExtruderState().add(extrudeLength);
			if(extruders[extruder].get5D())
				code += " E" + round(extruders[extruder].getExtruderState().length(), 1);
		}
		
		if (currentFeedrate != feedrate)
		{
			code += " F" + feedrate;
			currentFeedrate = feedrate;
		}
		
		code += " ;z move";
		gcode.queue(code);
		currentZ = z;	
	}



	/* (non-Javadoc)
	 * @see org.reprap.Printer#moveTo(double, double, double, boolean, boolean)
	 */
	public void moveTo(double x, double y, double z, double feedrate, boolean startUp, boolean endUp) throws Exception
	{
		if (isCancelled())
			return;

		x = round(x, 1);
		y = round(y, 1);
		z = round(z, 4);
		feedrate = round(feedrate, 1);
		
		double dx = x - currentX;
		double dy = y - currentY;
		double dz = z - currentZ;
		
		if (dx == 0.0 && dy == 0.0 && dz == 0.0)
			return;
		
		// This should either be a Z move or an XY move, but not all three
		
		boolean zMove = dz != 0;
		boolean xyMove = dx!= 0 || dy != 0;
		
		if(zMove && xyMove)
			Debug.d("GcodeRepRap.moveTo(): attempt to move in X|Y and Z simultaneously: (x, y, z) = (" + x + ", " + y + ", " + z + ")");

		double zFeedrate = round(getMaxFeedrateZ(), 1);
		
		double liftIncrement = extruders[extruder].getLift();  //extruders[extruder].getExtrusionHeight()/2;
		double liftedZ = round(currentZ + liftIncrement, 4);

		//go up first?
		if (startUp)
		{
			qZMove(liftedZ, zFeedrate);
			qFeedrate(feedrate);
		}
		
		if(dz > 0)
		{
			if(zMove)
				qZMove(z, feedrate);
			if(xyMove)
				qXYMove(x, y, feedrate);
		} else
		{
			if(xyMove)
				qXYMove(x, y, feedrate);
			if(zMove)
				qZMove(z, feedrate);			
		}
		
		if(endUp && !startUp)
		{
			qZMove(liftedZ, zFeedrate);
			qFeedrate(feedrate);			
		}
		
		if(!endUp && startUp)
		{
			qZMove(liftedZ - liftIncrement, zFeedrate);
			qFeedrate(feedrate);			
		}		
		super.moveTo(x, y, z, feedrate, startUp, endUp);
	}
	

	
	/**
	 * make a single, non building move (between plots, or zeroing an axis etc.)
	 */
	public void singleMove(double x, double y, double z, double feedrate)
	{
		double x0 = getX();
		double y0 = getY();
		double z0 = getZ();
		x = round(x, 1);
		y = round(y, 1);
		z = round(z, 4);
		double dx = x - x0;
		double dy = y - y0;
		double dz = z - z0;
		
		boolean zMove = dz != 0;
		boolean xyMove = dx != 0 || dy != 0;
		
		if(zMove && xyMove)
			Debug.d("GcodeRepRap.singleMove(): attempt to move in X|Y and Z simultaneously: (x, y, z) = (" + x + ", " + y + ", " + z + ")");
		
		try
		{
			if(xyMove && getExtruder().getMaxAcceleration() <= 0)
			{
				moveTo(x, y, z, feedrate, false, false);
				return;
			}

			if(xyMove)
			{
				double s = Math.sqrt(dx*dx + dy*dy);

				VelocityProfile vp = new VelocityProfile(s, getExtruder().getSlowXYFeedrate(), 
						feedrate, getExtruder().getSlowXYFeedrate(), getExtruder().getMaxAcceleration());
				switch(vp.flat())
				{
				case 0:
					qFeedrate(feedrate);
					moveTo(x, y, z0, feedrate, false, false);
					break;
					
				case 1:
					qFeedrate(getExtruder().getSlowXYFeedrate());
					moveTo(x0 + dx*vp.s1()/s, y0 + dy*vp.s1()/s, z0, vp.v(), false, false);
					moveTo(x, y, z0, getExtruder().getSlowXYFeedrate(), false, false);
					break;
					
				case 2:
					qFeedrate(getExtruder().getSlowXYFeedrate());
					moveTo(x0 + dx*vp.s1()/s, y0 + dy*vp.s1()/s, z0, feedrate, false, false);
					moveTo(x0 + dx*vp.s2()/s, y0 + dy*vp.s2()/s, z0, feedrate, false, false);
					moveTo(x, y, z0, getExtruder().getSlowXYFeedrate(), false, false);
					break;
					
				default:
					Debug.e("GCodeRepRap.singleMove(): dud VelocityProfile XY flat value.");	
				}
			}

			if(zMove)
			{
				VelocityProfile vp = new VelocityProfile(Math.abs(dz), getSlowZFeedrate(), 
						feedrate, getSlowZFeedrate(), getMaxZAcceleration());
				double s = 1;
				if(dz < 0)
					s = -1;
				switch(vp.flat())
				{
				case 0:
					qFeedrate(feedrate);
					moveTo(x0, y0, z, feedrate, false, false);
					break;
					
				case 1:
					qFeedrate(getSlowZFeedrate());
					moveTo(x0, y0, z0 + s*vp.s1(), vp.v(), false, false);
					moveTo(x0, y0, z, getSlowZFeedrate(), false, false);
					break;
					
				case 2:
					qFeedrate(getSlowZFeedrate());
					moveTo(x0, y0, z0 + s*vp.s1(), feedrate, false, false);
					moveTo(x0, y0, z0 + s*vp.s2(), feedrate, false, false);
					moveTo(x0, y0, z, getSlowZFeedrate(), false, false);
					break;
					
				default:
					Debug.e("GCodeRepRap.singleMove(): dud VelocityProfile Z flat value.");	
				}				
			}
		} catch (Exception e)
		{
			Debug.e(e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.reprap.Printer#printTo(double, double, double)
	 */
	public void printTo(double x, double y, double z, double feedrate, boolean stopExtruder, boolean closeValve) throws Exception
	{
		moveTo(x, y, z, feedrate, false, false);
		
		if(stopExtruder)
			getExtruder().stopExtruding();
		if(closeValve)
			getExtruder().setValve(false);
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.Printer#dispose()
	 */
	public void dispose() {
		// TODO: fix this to be more flexible
		
//		gcode.startingEpilogue();
//		
//		try
//		{
//			// Fan off
//			getExtruder().setCooler(false);
//			moveToFinish();
//			// Extruder off
//			getExtruder().setExtrusion(0, false);
//
//			// heater off
//			getExtruder().heatOff();
//		} catch(Exception e){
//			//oops
//		}
//		//write/close our file/serial port
//		gcode.reverseLayers();
//		gcode.finish();

		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.Printer#terminate()
	 */
	public void terminate() throws Exception
	{
		gcode.startingEpilogue();
		
		try
		{
			// Fan off
			//getExtruder().setCooler(false);
			moveToFinish();
			// Extruder off
			//getExtruder().setExtrusion(0, false);
			gcode.queue("M0 ;shut RepRap down");
			// heater off
			//getExtruder().heatOff();
		} catch(Exception e){
			//oops
		}
		//write/close our file/serial port
		gcode.reverseLayers();
		gcode.finish();
	}
	
	
	/**
	 * Go to the finish point
	 */
	public void moveToFinish()
	{
		//System.out.println("current: " + currentX + " " + currentY + " " + currentZ);
		//System.out.println("top: " + topX + " " + topY + " " + topZ);
		currentX = topX; // Nasty hack to deal with top-down computing of layers
		currentY = topY;
		currentZ = topZ;
		singleMove(currentX, currentY, currentZ + 1, getFastFeedrateZ());
		singleMove(getFinishX(), getFinishY(), currentZ, getExtruder().getFastXYFeedrate());
	}


	/* (non-Javadoc)
	 * @see org.reprap.Printer#initialise()
	 */
	public void startRun(LayerRules lc) throws Exception
	{	
		// If we are printing from a file, that should contain all the headers we need.
		if(gcode.buildingFromFile())
			return;
		
		gcode.startRun();
		
		gcode.queue("; GCode generated by RepRap Java Host Software");
		Date myDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
		String myDateString = sdf.format(myDate);
		gcode.queue("; Created: " + myDateString);
		gcode.queue(";#!RECTANGLE: " + lc.getBox());
		
		gcode.queue("M110 ; Reset the line numbers");
		//take us to fun, safe metric land.
		gcode.queue("G21 ;metric is good!");
		
		// Set absolute positioning, which is what we use.
		gcode.queue("G90 ;absolute positioning");
		
		currentX = 0;
		currentY = 0;
		currentZ = 0;
		currentFeedrate = -100; // Force it to set the feedrate at the start
		
		forceSelection = true;  // Force it to set the extruder to use at the start
		
		// Set the bed temperature
		
		setBedTemperature(bedTemperatureTarget);
				
		try	{
			super.startRun(lc);
		} catch (Exception E) {
			Debug.d("Initialization error: " + E.toString());
		}

	}

	/* (non-Javadoc)
	 * @see org.reprap.Printer#printStartDelay(long)
//	 */
//	public void printStartDelay(long msDelay) {
//		// This would extrude for the given interval to ensure polymer flow.
//		getExtruder().startExtruding();
//		
//		delay(msDelay);
//	}

	public void home() throws Exception {

		gcode.queue("G28; go home");
//		// Assume the extruder is off...
//		try
//		{
//			homeToZeroX();
//			homeToZeroY();
//			homeToZeroZ();
//		} catch (Exception e)
//		{
//			
//		}

		extruders[extruder].zeroExtrudedLength();
		super.home();
	}
	
	private void delay(long millis, boolean fastExtrude) throws Exception
	{
		double extrudeLength = getExtruder().getDistanceFromTime(millis);
		if(extrudeLength > 0)
		{
			if(extruders[extruder].get5D())
			{
				if(fastExtrude)
					qFeedrate(getExtruder().getFastEFeedrate());
				else
					qFeedrate(getExtruder().getFastXYFeedrate());
				// Fix the value for possible feedrate change
				extrudeLength = getExtruder().getDistanceFromTime(millis); 
			}

			if(extruders[extruder].getReversing())
				extruders[extruder].getExtruderState().add(-extrudeLength);
			else
				extruders[extruder].getExtruderState().add(extrudeLength);
			if(extruders[extruder].get5D())
			{
				String op = "G1 E" + round(extruders[extruder].getExtruderState().length(), 1);
				if(extruders[extruder].getReversing())
					op += "; extruder retraction";
				else
					op += "; extruder dwell";
				gcode.queue(op);
				qFeedrate(getExtruder().getSlowXYFeedrate());
				return;
			}
		}
		
		gcode.queue("G4 P" + millis + " ;delay");
	}
	
//	/**
//	 * Pick out a coordinate from the returned string
//	 * @param s
//	 * @param coord
//	 * @return
//	 */
//	private double getReturnedCoordinate(String s, String coord)
//	{
//		int i = s.indexOf(coord);
//		if(i < 0)
//			return 0;
//		String ss = s.substring(i + 1);
//		int j = ss.indexOf(" ");
//		return Double.parseDouble(ss.substring(0, j - 1));
//	}
	
	/**
	 * Are we paused?
	 * @return
	 */
	public boolean iAmPaused()
	{
		return gcode.iAmPaused();
	}
	
	/**
	 * Get X, Y, Z and E (if supported) coordinates in an array
	 * @return
	 * @throws Exception 
	 */
	public double[] getCoordinates() throws Exception
	{
		gcode.queue("M114; get coordinates");
		double [] result = new double[4];
		result[0] = gcode.getX();
		result[1] = gcode.getY();
		result[2] = gcode.getZ();
		result[3] = gcode.getE();
		return result;
	}
	
	/**
	 * Get X, Y, Z and E (if supported) coordinates in an array
	 * @return
	 * @throws Exception 
	 */
	public double[] getZeroError() throws Exception
	{
		gcode.queue("M117; get error coordinates");
		double [] result = new double[4];
		result[0] = gcode.getX();
		result[1] = gcode.getY();
		result[2] = gcode.getZ();
		result[3] = gcode.getE();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.reprap.Printer#homeToZeroX()
	 */
	public void homeToZeroX() throws Exception {

		
//		// Assume extruder is off...
//		try
//		{
//			singleMove(-250, currentY, currentZ, getExtruder().getFastXYFeedrate());
//		} catch (Exception e)
//		{}
//		gcode.queue("G92 X0 ;set x 0");
		gcode.queue("G28 X0 ;set x 0");
		super.homeToZeroX();
	}

	/* (non-Javadoc)
	 * @see org.reprap.Printer#homeToZeroY()
	 */
	public void homeToZeroY() throws Exception {

		// Assume extruder is off...
		
//		try
//		{
//			singleMove(currentX, -250, currentZ, getExtruder().getFastXYFeedrate());
//		} catch (Exception e)
//		{}
//		gcode.queue("G92 Y0 ;set y 0");
		gcode.queue("G28 Y0 ;set y 0");
		super.homeToZeroY();

	}
	
	public void homeToZeroXYE() throws Exception
	{
		if(XYEAtZero)
			return;
		homeToZeroX();
		homeToZeroY();
		int extruderNow = extruder;
		for(int i = 0; i < extruders.length; i++)
		{
			selectExtruder(i);
			extruders[i].zeroExtrudedLength();
		}
		selectExtruder(extruderNow);
		XYEAtZero = true;
		super.homeToZeroXYE();
	}

	/* (non-Javadoc)
	 * @see org.reprap.Printer#homeToZeroY()
	 */
	public void homeToZeroZ() throws Exception {

		// Assume extruder is off...
//		try
//		{
//			singleMove(currentX, currentY, -250, getMaxFeedrateZ());
//		} catch (Exception e)
//		{}
//		gcode.queue("G92 Z0 ;set z 0");
		gcode.queue("G28 Z0 ;set z 0");
		super.homeToZeroZ();
	}
	
	public static double round(double c, double d)
	{
		double power = Math.pow(10.0, d);
		
		return Math.round(c*power)/power;
	}
	
	public void waitTillNotBusy() throws IOException {}

	//TODO: make this work normally.
	public void stopMotor() throws Exception
	{
		getExtruder().stopExtruding();
	}
	
	//TODO: make this work normally.
	public void stopValve() throws Exception
	{
		getExtruder().setValve(false);
	}
	
	/**
	 * All machine dwells and delays are routed via this function, rather than 
	 * calling Thread.sleep - this allows them to generate the right G codes (G4) etc.
	 * 
	 * The RS232/USB etc comms system doesn't use this - it sets its own delays.
	 * @param milliseconds
	 * @throws Exception 
	 */
	public void machineWait(double milliseconds, boolean fastExtrude) throws Exception
	{
		if(milliseconds <= 0)
			return;
		delay((long)milliseconds, fastExtrude);
	}
	
	/**
	 * Wait until the GCodeWriter has exhausted its buffer.
	 */
	public void waitWhileBufferNotEmpty()
	{
//		while(!gcode.bufferEmpty())
//			gcode.sleep(97);
	}
	
	public void slowBuffer()
	{
		gcode.slowBufferThread();
	}
	
	public void speedBuffer()
	{
		gcode.speedBufferThread();
	}
	
	/**
	 * Load a GCode file to be made.
	 * @return the name of the file
	 */
	public String loadGCodeFileForMaking()
	{
		super.loadGCodeFileForMaking();
		return gcode.loadGCodeFileForMaking();
	}
	
	/**
	 * Set an output file
	 * @return
	 */
	public String setGCodeFileForOutput(String fileRoot)
	{
		return gcode.setGCodeFileForOutput(getTopDown(), fileRoot);
	}
	
	/**
	 * If a file replay is being done, do it and return true
	 * otherwise return false.
	 * @return
	 */
	public boolean filePlay()
	{
		return gcode.filePlay();
	}
	
	/**
	 * Stop the printer building.
	 * This _shouldn't_ also stop it being controlled interactively.
	 */
	public void pause()
	{
		gcode.pause();
	}
	
	/**
	 * Resume building.
	 *
	 */
	public void resume()
	{
		gcode.resume();
	}
	
	public void startingLayer(LayerRules lc) throws Exception
	{
		currentFeedrate = -1;  // Force it to set the feedrate
		gcode.startingLayer(lc);
		gcode.queue(";#!LAYER: " + (lc.getMachineLayer() + 1) + "/" + lc.getMachineLayerMax());
		super.startingLayer(lc);
	}
	
	/**
	 * Tell the printer class it's Z position.  Only to be used if
	 * you know what you're doing...
	 * @param z
	 */
	public void setZ(double z)
	{
		currentZ = round(z, 4);
	}
	
	public void finishedLayer(LayerRules lc) throws Exception
	{
		super.finishedLayer(lc);
		gcode.finishedLayer();
	}
	
	public void selectExtruder(int materialIndex) throws Exception
	{
		int oldPhysicalExtruder = getExtruder().getPhysicalExtruderNumber();
		super.selectExtruder(materialIndex);
		int newPhysicalExtruder = getExtruder().getPhysicalExtruderNumber();
		if(newPhysicalExtruder != oldPhysicalExtruder || forceSelection)
		{
			gcode.queue("T" + newPhysicalExtruder + "; select new extruder");
			double pwm = getExtruder().getPWM();
			if(pwm >= 0)
				gcode.queue("M113 S" + pwm + "; set extruder PWM");
			else
				gcode.queue("M113; set extruder to use pot for PWM");
			forceSelection = false;
		}
	}
	
	
	/**
	 * Set the bed temperature. This value is given
	 * in centigrade, i.e. 100 equals 100 centigrade. 
	 * @param temperature The temperature of the extruder in centigrade
	 * @param wait - wait till it gets there (or not).
	 * @throws Exception 
	 * @throws Exception
	 */
	public void setBedTemperature(double temperature) throws Exception
	{
		super.setBedTemperature(temperature);
		gcode.queue("M140 S" + temperature + " ;set bed temperature and return");
	}
	
	/**
	 * Return the current temperature of the bed
	 * @return
	 * @throws Exception 
	 */
	public double getBedTemperature() throws Exception
	{ 
		gcode.queue("M105; get temperature");
		return gcode.getBTemp();
	}
	
	/**
	 * Wait till the entire machine is ready to print.  That is that such things as
	 * extruder and bed temperatures are at the values set and stable.
	 * @throws Exception 
	 */
	public void stabilise() throws Exception
	{
		gcode.queue("M116 ;wait for stability then return");
	}
}
