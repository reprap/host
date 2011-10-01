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

import org.reprap.Extruder;
import org.reprap.Preferences;
import org.reprap.comms.GCodeReaderAndWriter;
import org.reprap.utilities.Debug;
import org.reprap.devices.GCodeExtruder;
import org.reprap.geometry.LayerRules;
import org.reprap.geometry.polygons.Point2D;
import java.io.IOException;
import java.io.PrintStream;
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
		String s = "M110";
		if(Debug.d())
			s += " ; Reset the line numbers";
		gcode.queue(s);
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
		String s = "G1 F" + feedrate;
		if(Debug.d())
			s += " ; feed for start of next move";
		gcode.queue(s);
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
		String s = "G1 ";

		if (dx != 0)
			s += "X" + x;
		if (dy != 0)
			s += " Y" + y;

		extrudeLength = extruders[extruder].getDistance(Math.sqrt(dx*dx + dy*dy));

		if(extrudeLength > 0)
		{
			if(extruders[extruder].getReversing())
				extruders[extruder].getExtruderState().add(-extrudeLength);
			else
				extruders[extruder].getExtruderState().add(extrudeLength);
			if(extruders[extruder].get5D())
				s += " E" + round(extruders[extruder].getExtruderState().length(), 1);
		}
		
		if (currentFeedrate != feedrate)
		{
			s += " F" + feedrate;
			currentFeedrate = feedrate;
		}
		
		if(Debug.d())
			s += " ;horizontal move";
		gcode.queue(s);
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
		
		String s = "G1 Z" + z;

		extrudeLength = extruders[extruder].getDistance(dz);

		if(extrudeLength > 0)
		{
			if(extruders[extruder].getReversing())
				extruders[extruder].getExtruderState().add(-extrudeLength);
			else
				extruders[extruder].getExtruderState().add(extrudeLength);
			if(extruders[extruder].get5D())
				s += " E" + round(extruders[extruder].getExtruderState().length(), 1);
		}
		
		if (currentFeedrate != feedrate)
		{
			s += " F" + feedrate;
			currentFeedrate = feedrate;
		}
		
		if(Debug.d())
			s += " ;z move";
		gcode.queue(s);
		currentZ = z;	
	}



	/* (non-Javadoc)
	 * @see org.reprap.Printer#moveTo(double, double, double, boolean, boolean)
	 */
	public void moveTo(double x, double y, double z, double feedrate, boolean startUp, boolean endUp) throws Exception
	{
		if (isCancelled())
			return;
		
		try
		{
		if(x > Preferences.loadGlobalDouble("WorkingX(mm)") || x < 0)
		{
			Debug.e("Attempt to move x to " + x + " which is outside [0, " + Preferences.loadGlobalDouble("WorkingX(mm)") + "]");
			x = Math.max(0, Math.min(x, Preferences.loadGlobalDouble("WorkingX(mm)")));
		}
		if(y > Preferences.loadGlobalDouble("WorkingY(mm)") || y < 0)
		{
			Debug.e("Attempt to move y to " + y + " which is outside [0, " + Preferences.loadGlobalDouble("WorkingY(mm)") + "]");
			y = Math.max(0, Math.min(y, Preferences.loadGlobalDouble("WorkingY(mm)")));
		}
		if(z > Preferences.loadGlobalDouble("WorkingZ(mm)") || z < 0)
		{
			Debug.e("Attempt to move z to " + z + " which is outside [0, " + Preferences.loadGlobalDouble("WorkingZ(mm)") + "]");
			z = Math.max(0, Math.min(z, Preferences.loadGlobalDouble("WorkingZ(mm)")));
		}
		} catch (Exception e)
		{}

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
			Debug.d("GcodeRepRap.moveTo(): attempt to move in X|Y and Z simultaneously: (x, y, z) = (" + 
					currentX + "->" + x + ", " + 
					currentY + "->" + y + ", " +
					currentZ + "->" + z + ", " +
					")");

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
	public void singleMove(double x, double y, double z, double feedrate, boolean really)
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
		
		if(!really)
		{
			currentX = x;
			currentY = y;
			currentZ = z;
			currentFeedrate = feedrate;
			return;
		}
		
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
	
	/**
	 * make a single, non building move (between plots, or zeroing an axis etc.) when we know it starts somewhere
	 * other than the current position.
	 */
/*	public void singleMove(double x0, double y0, double z0, double x, double y, double z, double feedrate)
	{
		currentX = round(x0, 1);
		currentY = round(y0, 1);
		currentZ = round(z0, 4);
		singleMove(x, y, z, feedrate);
	}*/

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
	

	
	
	/**
	 * Go to the finish point
	 */
	public void moveToFinish(LayerRules lc)
	{
		currentFeedrate = -100; // Force it to set the feedrate
		singleMove(getX(), getY(), getZ() + 1, getFastFeedrateZ(), lc.getReversing());
		singleMove(getFinishX(), getFinishY(), getZ(), getExtruder().getFastXYFeedrate(), lc.getReversing());
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
		
		String s = "M110";
		if(Debug.d())
			s += " ; Reset the line numbers";
		gcode.queue(s);
		//take us to fun, safe metric land.
		s = "G21";
		if(Debug.d())
			s += " ; metric is good!";
		gcode.queue(s);
		
		// Set absolute positioning, which is what we use.
		s = "G90";
		if(Debug.d())
			s += " ; absolute positioning";		
		gcode.queue(s);
		
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
	
	public void startingLayer(LayerRules lc) throws Exception
	{
		currentFeedrate = -1;  // Force it to set the feedrate
		gcode.startingLayer(lc);
		if(lc.getReversing())
			gcode.queue(";#!LAYER: " + (lc.getMachineLayer() + 1) + "/" + lc.getMachineLayerMax());
		super.startingLayer(lc);
	}
	
	public void finishedLayer(LayerRules lc) throws Exception
	{
		super.finishedLayer(lc);
		gcode.finishedLayer(lc);
	}
	
	/* (non-Javadoc)
	 * @see org.reprap.Printer#terminate(LayerRules layerRules)
	 */
	public void terminate(LayerRules lc) throws Exception
	{
		gcode.startingEpilogue(lc);
		
		int topLayer = lc.realTopLayer();
		try
		{
			// Fan off
			//getExtruder().setCooler(false);
			Point2D p = lc.getLastPoint(topLayer);
			currentX = round(p.x(),1);
			currentY = round(p.y(),1);
			//System.out.println("final XY: " + currentX + ", " + currentY);
			currentZ = round(lc.getLayerZ(topLayer),1);
			//gcode.queue("; Moving to finish:");
			moveToFinish(lc);
			// Extruder off
			//getExtruder().setExtrusion(0, false);
			String s = "M0";
			if(Debug.d())
				s += " ; shut RepRap down";
			gcode.queue(s);
			// heater off
			//getExtruder().heatOff();
		} catch(Exception e){
			//oops
		}
		//write/close our file/serial port
		//gcode.reverseLayers(layerRules);
		gcode.finish(lc);
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

	public void home() throws Exception 
	{
		String s = "G28";
		if(Debug.d())
			s += " ; go home";
		gcode.queue(s);
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

		extruders[extruder].zeroExtrudedLength(true);
		super.home();
	}
	
	private void delay(long millis, boolean fastExtrude, boolean really) throws Exception
	{
		double extrudeLength = getExtruder().getDistanceFromTime(millis);
		
		String s;
		
		if(extrudeLength > 0)
		{
			if(extruders[extruder].get5D())
			{
				double fr;
				if(fastExtrude)
					fr = getExtruder().getFastEFeedrate();
				else
					fr = getExtruder().getFastXYFeedrate();
				if(really)
					qFeedrate(fr);
				else
					currentFeedrate = fr;
				// Fix the value for possible feedrate change
				extrudeLength = getExtruder().getDistanceFromTime(millis); 
			}

			if(extruders[extruder].getReversing())
				extruders[extruder].getExtruderState().add(-extrudeLength);
			else
				extruders[extruder].getExtruderState().add(extrudeLength);
			
			
			if(extruders[extruder].get5D())
			{
				s = "G1 E" + round(extruders[extruder].getExtruderState().length(), 1);
				if(Debug.d())
				{
					if(extruders[extruder].getReversing())
						s += " ; extruder retraction";
					else
						s += " ; extruder dwell";
				}
				if(really)
				{
					gcode.queue(s);
					qFeedrate(getExtruder().getSlowXYFeedrate());
				} else
					currentFeedrate = getExtruder().getSlowXYFeedrate();
				return;
			}
		}
		
		s = "G4 P" + millis;
		if(Debug.d())
			s += " ; delay";
		gcode.queue(s);
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
		String s = "M114";
		if(Debug.d())
			s += " ; get coordinates";
		gcode.queue(s);
		double [] result = new double[4];
		result[0] = gcode.getX();
		result[1] = gcode.getY();
		result[2] = gcode.getZ();
		result[3] = gcode.getE();
		return result;
	}
	
	/**
	 * Get the RepRap's SD card (if any) online
	 */
	public void initialiseSD()
	{
		String s = "M21";
		if(Debug.d())
			s += " ; Initialise SD card";
		try {
			gcode.queue(s);
		} catch (Exception e) {
			Debug.e("GCodeRepRap.initialiseSD() has thrown:");
			e.printStackTrace();
		}		
	}
	
	/**
	 * Get the file list from the machine's SD card
	 * @return
	 */
	public String[] getSDFiles()
	{
		initialiseSD();
		String s = "M20";
		if(Debug.d())
			s += " ; get file list";
		try {
			gcode.queue(s);
		} catch (Exception e) {
			Debug.e("GCodeRepRap.getSDFiles() has thrown:");
			e.printStackTrace();
		}
		return gcode.getSDFileNames();	
	}
	
	/**
	 * Print a file on the SD card
	 * @param filename
	 */
	public void printSDFile(String filename)
	{
		String s = "M23 " + filename;
		if(Debug.d())
			s += " ; Send SD name to print";
		try {
			gcode.queue(s);
		} catch (Exception e) {
			Debug.e("GCodeRepRap.printSDFile() has thrown:");
			e.printStackTrace();
		}
		s = "M24";
		if(Debug.d())
			s += " ; Start print from SD";
		try {
			gcode.queue(s);
		} catch (Exception e) {
			Debug.e("GCodeRepRap.printSDFile() has thrown:");
			e.printStackTrace();
		}
	}
	
	/**
	 * Get X, Y, Z and E (if supported) coordinates in an array
	 * @return
	 * @throws Exception 
	 */
	public double[] getZeroError() throws Exception
	{
		String s = "M117";
		if(Debug.d())
			s += " ; get error coordinates";
		gcode.queue(s);
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
		String s = "G28 X0";
		if(Debug.d())
			s += " ; set x 0";
		gcode.queue(s);
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
		String s = "G28 Y0";
		if(Debug.d())
			s += " ; set y 0";
		gcode.queue(s);
		super.homeToZeroY();

	}
	
	public void homeToZeroXYE(boolean really) throws Exception
	{
		if(XYEAtZero)
			return;
		if(really)
		{
			homeToZeroX();
			homeToZeroY();
		} else
		{
			currentX = 0;
			currentY = 0;
		}
		int extruderNow = extruder;
		for(int i = 0; i < extruders.length; i++)
		{
			selectExtruder(i, really);
			extruders[i].zeroExtrudedLength(really);
		}
		selectExtruder(extruderNow, really);
		XYEAtZero = true;
		super.homeToZeroXYE(really);
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
		String s = "G28 Z0";
		if(Debug.d())
			s += " ; set z 0";
		gcode.queue(s);
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
	public void machineWait(double milliseconds, boolean fastExtrude, boolean really) throws Exception
	{
		if(milliseconds <= 0)
			return;
		delay((long)milliseconds, fastExtrude, really);
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
	public Thread filePlay()
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
		
	/**
	 * Tell the printer class it's Z position.  Only to be used if
	 * you know what you're doing...
	 * @param z
	 */
	public void setZ(double z)
	{
		currentZ = round(z, 4);
	}
	

	
	public void selectExtruder(int materialIndex, boolean really) throws Exception
	{
		int oldPhysicalExtruder = getExtruder().getPhysicalExtruderNumber();
		super.selectExtruder(materialIndex, true);
		int newPhysicalExtruder = getExtruder().getPhysicalExtruderNumber();
		if(newPhysicalExtruder != oldPhysicalExtruder || forceSelection)
		{
			if(really)
			{
				String s = "T" + newPhysicalExtruder;
				if(Debug.d())
					s += " ; select new extruder";
				gcode.queue(s);
				double pwm = getExtruder().getPWM();
				if(pwm >= 0)
				{
					s = "M113 S" + pwm;
					if(Debug.d())
						s += " ; set extruder PWM";
					gcode.queue(s);
				}else
				{
					s = "M113";
					if(Debug.d())
						s += " ; set extruder to use pot for PWM";
					gcode.queue(s);
				}
			}
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
		String s = "M140 S" + temperature;
		if(Debug.d())
			s += " ; set bed temperature and return";
		gcode.queue(s);
	}
	
	/**
	 * Return the current temperature of the bed
	 * @return
	 * @throws Exception 
	 */
	public double getBedTemperature() throws Exception
	{ 
		String s = "M105";
		if(Debug.d())
			s += " ; get temperature";
		gcode.queue(s);
		return gcode.getBTemp();
	}
	
	/**
	 * Wait till the entire machine is ready to print.  That is that such things as
	 * extruder and bed temperatures are at the values set and stable.
	 * @throws Exception 
	 */
	public void stabilise() throws Exception
	{
		String s = "M116";
		if(Debug.d())
			s += " ; wait for stability then return";
		gcode.queue(s);
	}
	
	/**
	 * Force the output stream to be some value - use with caution
	 * @param fos
	 */
	public void forceOutputFile(PrintStream fos)
	{
		gcode.forceOutputFile(fos);
	}
	
	/**
	 * Return the name if the gcode file
	 * @return
	 */
	public String getOutputFilename()
	{
		return gcode.getOutputFilename();
	}
}
