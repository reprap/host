package org.reprap;

import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import org.reprap.geometry.LayerRules;
//import org.reprap.devices.GenericStepperMotor;

public interface Printer {
	
	/**
	 * (Re)load the preferences
	 *
	 */
	public void refreshPreferences();

	/**
	 * Method to calibrate the printer.
	 */
	public void calibrate();

//	public void printSegment(double startX, double startY,
//			double startZ, double endX, double endY, double endZ) throws ReprapException, IOException;
	
	
	/**
	 * Move the printer carriage to the give x, y and z position <b>while extruding material<b>
	 * 
	 * @param x absolute x position in millimeters relative to the home position. 
	 * Range between [0..???]
	 * @param y absolute y position in millimters relative to the home position. 
	 * Range betwen [0..???]
	 * @param z absolute z position in millimters relative to the home position.
	 * Range between [0..???]
	 * @param startUp ?
	 * @param endUp ?
	 * @throws ReprapException
	 * @throws IOException 
	 * @throws Exception 
	 */
	public void moveTo(double x, double y, double z, double feedrate, boolean startUp, boolean endUp) throws ReprapException, IOException, Exception;
	
	/**
	 * Single move for when we're moving about giong places rather than making
	 * @param x
	 * @param y
	 * @param z
	 * @param feedrate
	 */
	public void singleMove(double x, double y, double z, double feedrate);
	
	/**
	 * Move the printer carriage to the give x, y and z position <b>while extruding material<b>
	 * 
	 * @param x absolute x position in millimeters relative to the home position. 
	 * Range between [0..???]
	 * @param y absolute y position in millimters relative to the home position. 
	 * Range betwen [0..???]
	 * @param z absolute z position in millimters relative to the home position.
	 * Range between [0..???]
	 * @param lastOne True if extruder should be turned off at end of this segment.
	 * @throws ReprapException
	 * @throws IOException 
	 * @throws Exception 
	 */
	public void printTo(double x, double y, double z, double feedrate, boolean stopExtruder, boolean closeValve) throws ReprapException, IOException, Exception;
	
	/**
	 * Get the feedrate currently being used
	 * @return
	 */
	public double getCurrentFeedrate();
	
	/**
	 * Fire up the extruder for a lead-in
	 * @param firstOneInLayer (first after the layer pause, or any old polygon?)
	 */
	public void printStartDelay(boolean firstOneInLayer);	
	
	/**
	 * Maybe reverse the extruder at the end of a track
	 */
	public void printEndReverse();
	
	/**
	 * Home all axes
	 * @throws Exception 
	 *
	 */
	public void home() throws Exception;
	
	/**
	 * Sync to zero X location.
	 * @throws ReprapException
	 * @throws IOException
	 * @throws Exception 
	 */
	public void homeToZeroX() throws ReprapException, IOException, Exception;
	
	/**
	 * Sync to zero Y location.
	 * @throws ReprapException
	 * @throws IOException
	 * @throws Exception 
	 */
	public void homeToZeroY() throws ReprapException, IOException, Exception; 
	
	
	/**
	 * Home XY and zero all extruders
	 * @throws ReprapException
	 * @throws IOException
	 * @throws Exception 
	 */
	public void homeToZeroXYE() throws ReprapException, IOException, Exception; 
	
	/**
	 * Sync to zero Z location.
	 * @throws ReprapException
	 * @throws IOException
	 * @throws Exception 
	 */
	public void homeToZeroZ() throws ReprapException, IOException, Exception; 
	
	/**
	 * Select a specific material to print with
	 * @param attributes with name of the material
	 * @throws Exception 
	 */
	public void selectExtruder(Attributes att) throws Exception;
	
	/**
	 * Select a specific material to print with
	 * @param extr identifier of the material
	 * @throws Exception 
	 */
	public void selectExtruder(int extr) throws Exception;
	
	/**
	 * Start a production run (as opposed to moving the machine about
	 * interactively, for example).
	 */
	public void startRun(LayerRules lc) throws Exception;
	
	/**
	 * Indicates end of job, homes extruder, powers down etc
	 * @throws Exception
	 */
	public void terminate() throws Exception;
	
	/**
	 * Dispose of the printer
	 */
	public void dispose();
	
	
	/**
	 * @param feedrate in mm/minute
	 */
	//public void setFeedrate(double feedrate);
	
	/**
	 * @param feedrate in mm/minute
	 */
//	public void setFastFeedrateXY(double feedrate);
	
	/**
	 * @return fast XY movement feedrate in mm/minute
	 */
	public double getFastXYFeedrate();
	
	/**
	 * @return slow XY movement feedrate in mm/minute
	 */
	public double getSlowXYFeedrate();
	
	/**
	 * @return slow Z movement feedrate in mm/minute
	 */
	public double getSlowZFeedrate();	
	
	/**
	 * @return the fastest the machine can accelerate
	 */
	public double getMaxXYAcceleration();
	
	/**
	 * @return the fastest the machine can accelerate
	 */
	public double getMaxZAcceleration();
	
	/**
	 * @param feedrate in mm/minute
	 */
	//public void setFastFeedrateZ(double feedrate);
	
	/**
	 * @return the extruder feedrate in mm/minute
	 */
	public double getFastFeedrateZ();
	
	/**
	 * The location of the dump for purging extruders
	 * @return
	 */
	public double getDumpX();
	public double getDumpY();
	
	/**
	 * The location to go to at the end
	 * @return
	 */
	public double getFinishX();
	public double getFinishY();
	
	/**
	 * Move to the purge point
	 *
	 */
	public void moveToPurge(boolean raiseZ);
	
	/**
	 * @param previewer
	 */
	//public void setPreviewer(Previewer previewer);
	
	/**
	 * @return is cancelled when ...?
	 */
	public boolean isCancelled();
	
//	/**
//	 * @throws Exception
//	 */
//	public void initialise() throws Exception;
	
	/**
	 * @return current X position
	 */
	public double getX();
	
	/**
	 * @return current Y position
	 */
	public double getY();
	
	/**
	 * @return current Z position
	 */
	public double getZ();
	
	/**
	 * Get X, Y, Z and E (if supported) coordinates in an array
	 * @return
	 * @throws Exception 
	 */
	public double[] getCoordinates() throws Exception;
	
	/**
	 * Get the zero errors
	 * @return
	 * @throws Exception
	 */
	public double[] getZeroError() throws Exception;
	
	/**
	 * Tell the printer class it's Z position.  Only to be used if
	 * you know what you're doing...
	 * @param z
	 */
	public void setZ(double z);
	
	/**
	 * Set the position at the end of the topmost layer
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setTop(double x, double y, double z);
	
	/**
	 * The next time an extruder is changed, force the change to be
	 * sent to the RepRap, even if it's changed to the same as it just was.
	 */
	public void forceNextExtruder();

	/**
	 * @return the extruder currently in use
	 */
	public Extruder getExtruder();
	
	/**
	 * @return the index of the extruder currently in use
	 */
	public int getExtruderNumber();
	
	/**
	 * @param name
	 * @return the extruder for the material called name; null if not found.
	 */
	public Extruder getExtruder(String name);
	
	/**
	 * Get the list of all the extruders
	 * @return
	 */
	public Extruder[] getExtruders();
	
	/**
	 * Stop the extrude motor (if any)
	 * @throws IOException
	 * @throws Exception 
	 */
	public void stopMotor() throws IOException, Exception;
	
	/**
	 * Close the extrude valve (if any)
	 * @throws IOException
	 * @throws Exception 
	 */
	public void stopValve() throws IOException, Exception;

	/**
	 * Allow the user to manually calibrate the Z axis position to deal
	 * with special circumstances like different extruder sizes, platform
	 * additions or subtractive fabrication.
	 * 
	 * @throws IOException
	 */
	public void setZManual() throws IOException;

	/**
	 * Allow the user to manually calibrate the Z axis position to deal
	 * with special circumstances like different extruder sizes, platform
	 * additions or subtractive fabrication.
	 * 
	 * @param zeroPoint The point the user selects will be treated as the
	 * given Z value rather than 0.0 
	 */
	public void setZManual(double zeroPoint) throws IOException;
	
	/**
	 * Get the total distance moved (whether extruding or not)
	 * @return a double representing the distance travelled (mm)
	 */
	public double getTotalDistanceMoved();
	
	/**
	 * Get the total distance moved while extruding
	 * @return a double representing the distance travelled (mm)
	 */
	public double getTotalDistanceExtruded();

	/**
	 * @return total time the extruder has been moving in seconds
	 */
	public double getTotalElapsedTime();
	
//	/**
//	 * The bits of the parts made so for for the simulation
//	 * @param ls
//	 */
//	public void setLowerShell(BranchGroup ls);
	
	
	/**
	 * Just finished a layer. Do whatever needs to be done then.
	 * @param layerNumber
	 */
	public void finishedLayer(LayerRules lc) throws Exception;
	
	/**
	 * Do whatever needs to be done between one layer and the next
	 * @param layerNumber
	 */
	public void betweenLayers(LayerRules lc) throws Exception;
	
	/**
	 * Just about to start the next layer
	 * @param layerNumber
	 */
	public void startingLayer(LayerRules lc) throws Exception;
	
	/**
	 * Set the source checkbox used to determine if there should
	 * be a pause between segments.
	 * 
	 * @param segmentPause The source checkbox used to determine
	 * if there should be a pause.  This is a checkbox rather than
	 * a boolean so it can be changed on the fly. 
	 */
	public void setSegmentPause(JCheckBoxMenuItem segmentPause);
	
	/**
	 * Set the source checkbox used to determine if there should
	 * be a pause between layers.
	 * 
	 * @param layerPause The source checkbox used to determine
	 * if there should be a pause.  This is a checkbox rather than
	 * a boolean so it can be changed on the fly.
	 */
	public void setLayerPause(JCheckBoxMenuItem layerPause);
	
	/**
	 * How many layers of foundation to build
	 * @return
	 */
	public int getFoundationLayers();
	
	/**
	 * Tell the printer it's been cancelled
	 * @param c
	 */
	public void setCancelled(boolean c);
	
	/**
	 * Wait while the motors move about
	 * @throws IOException
	 */
	public void waitTillNotBusy() throws IOException;
	
	/**
	 * Get the X stepper
	 * @return
	 */
//	public GenericStepperMotor getXMotor();
	
	/**
	 * Get the Y stepper
	 * @return
	 */
//	public GenericStepperMotor getYMotor();
	
	/**
	 * Get the Z stepper
	 * @return
	 */
//	public GenericStepperMotor getZMotor();
	
//	/**
//	 * Convert XY feedrates in mm/min to internal units
//	 * @param feedrate
//	 * @return
//	 */
//	public int convertFeedrateToSpeedXY(double feedrate);
	
//	/**
//	 * Convert Z feedrates in mm/min to internal units
//	 * @param feedrate
//	 * @return
//	 */	
//	public int convertFeedrateToSpeedZ(double feedrate);
	
//	/**
//	 * The X discretisation
//	 * @return
//	 */
//	public double getXStepsPerMM();
//	
//	/**
//	 * The Y discretisation
//	 * @return
//	 */
//	public double getYStepsPerMM();
//	
//	/**
//	 * The Z discretisation
//	 * @return
//	 */
//	public double getZStepsPerMM();
//	
	/**
	 * If we are using an output buffer, it's a good idea to wait till
	 * it's empty between layers before computing the next one.
	 */
	public void waitWhileBufferNotEmpty();
	
	/**
	 * It's also useful to be able to change its priority
	 *
	 */
	public void slowBuffer();
	public void speedBuffer();
	
	/**
	 * All machine dwells and delays are routed via this function, rather than 
	 * calling Thread.sleep - this allows them to generate the right G codes (G4) etc.
	 * 
	 * The RS232/USB etc comms system doesn't use this - it sets its own delays.
	 * @param milliseconds
	 * @throws Exception 
	 */
	public void machineWait(double milliseconds, boolean fastExtrude) throws Exception;
	
	/**
	 * Load a file to be made.
	 * Currently these can be STLs (more than one can be loaded) or
	 * a GCode file, or an .rfo file.
	 * @return the name of the file
	 */
	public String addSTLFileForMaking();
	public String loadGCodeFileForMaking();
	public String loadRFOFileForMaking();
	public String saveRFOFile(String fileRoot);
	
	/**
	 * Set an output file
	 * @return
	 */
	public String setGCodeFileForOutput(String fileRoot);
	
	/**
	 * If a file replay is being done, do it and return true
	 * otherwise return false.
	 * @return
	 */
	public boolean filePlay();
	
	/**
	 * Stop the printer building.
	 * This _shouldn't_ also stop it being controlled interactively.
	 */
	public void pause();
	
	
	/**
	 * Query the paused status
	 * @return
	 */
	public boolean iAmPaused();
	
	/**
	 * Resume building.
	 *
	 */
	public void resume();
	
	/**
	 * Set the flag that decided which direction to compute the layers
	 * @param td
	 */
	public void setTopDown(boolean td);
	
	/**
	 * @return the flag that decided which direction to compute the layers
	 */
	public boolean getTopDown();
	
	/**
	 * Set all the extruders' separating mode
	 * @param s
	 */
	public void setSeparating(boolean s);
	
	
	/**
	 * Set the bed temperature. This value is given
	 * in centigrade, i.e. 100 equals 100 centigrade. 
	 * @param temperature The temperature of the extruder in centigrade
	 * @param wait - wait till it gets there (or not).
	 * @throws Exception 
	 * @throws Exception
	 */
	public void setBedTemperature(double temperature) throws Exception;
	
	/**
	 * Return the current temperature of the bed
	 * @return
	 * @throws Exception 
	 */
	public double getBedTemperature() throws Exception;
	
	/**
	 * The temperature we want the bed to be at
	 * @return
	 */
	public double getBedTemperatureTarget();
	
	/**
	 * Wait till the entire machine is ready to print.  That is that such things as
	 * extruder and bed temperatures are at the values set and stable.
	 * @throws Exception 
	 */
	public void stabilise() throws Exception;
}
