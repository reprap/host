package org.reprap;

import java.io.IOException;
import org.reprap.devices.ExtruderState;
import javax.media.j3d.Appearance;

public interface Extruder
{
	
	/**
	 * Dispose of the extruder object 
	 */
	public void dispose(); 
	
	/**
	 * Reload the preferences from the preferences file
	 *
	 */
	public int refreshPreferences();

	/**
	 * Start the extruder motor at a given speed.  For old extruders this ranges from 0
	 * to 255 but is scaled by maxSpeed and t0, so that 255 corresponds to the
	 * highest permitted speed.  It is also scaled so that 0 would correspond
	 * with the lowest extrusion speed.
	 * For new extruders this is in mm/minute
	 * @param speed The speed to drive the motor at (0-255)
	 * @throws IOException
	 */
	//public void setExtrusion(double speed) throws IOException;
	
	/**
	 * For stepper extruders, return the PWM to use to drive them in [0, 1]
	 * Negative values returned mean this feature is disabled.
	 */
	public double getPWM();
	
	/**
	 * Start the extruder motor at a given speed.  For old extruders this ranges from 0
	 * to 255 but is scaled by maxSpeed and t0, so that 255 corresponds to the
	 * highest permitted speed.  It is also scaled so that 0 would correspond
	 * with the lowest extrusion speed.
	 * For new extruders this is in mm/minute
	 * @param speed The speed to drive the motor at (0-255)
	 * @param reverse If set, run extruder in reverse
	 * @throws IOException
	 * @throws Exception 
	 */
	public void setExtrusion(double speed, boolean reverse) throws IOException, Exception;
	
	/**
	* start extruding at the normal rate.
	*/
	public void startExtruding() throws Exception;
	
	/**
	* stop extruding
	*/
	public void stopExtruding() throws Exception;
	
	/**
	* Start/stop the extruder motor
	 * @throws Exception 
	*/
	public void setMotor(boolean motorOn) throws IOException, Exception;
		
	/**
	 * Open and close the valve (if any).
	 * @param pulseTime
	 * @param valveOpen
	 * @throws IOException
	 * @throws Exception 
	 */
	public void setValve(boolean valveOpen) throws IOException, Exception;
	
	/**
	 * Turn the heater of the extruder on. Inital temperatur is defined by ???
	 * @throws Exception
	 */
	public void heatOn(boolean wait) throws Exception; 

	/**
	* Turns the heater for the extruder off.
	*/
	public void heatOff() throws Exception; 

	/**
	 * Set the temperature of the extruder at a given height. This height is given
	 * in centigrades, i.e. 100 equals 100 centigrades. 
	 * @param temperature The temperature of the extruder in centigrade
	 * @param wait - wait till it gets there (or not).
	 * @throws Exception
	 */
	public void setTemperature(double temperature, boolean wait) throws Exception; 
	
	/**
	 * Set a heat output power.  For normal production use you would
	 * normally call setTemperature, however this method may be useful
	 * for lower temperature profiling, etc.
	 * @param heat Heater power (0-255)
	 * @param maxTemp Cutoff temperature in celcius
	 * @throws IOException
	 */
	public void setHeater(int heat, double maxTemp) throws IOException; 
	
	/**
	 * Check if the extruder is out of feedstock
	 * @return true if there is no material remaining
	 */
	public boolean isEmpty(); 
	
	/**
	 * @return the target temperature of the extruder
	 */
	public double getTemperatureTarget(); 

	/**
	 * @return the default temperature of the extruder
	 */
	public double getDefaultTemperature();

	/**
	 * @return the current temperature of the extruder 
	 */
	public double getTemperature() throws Exception; 

	/**
	 * @return the infill speed as a value between [0,1]
	 */
	public double getInfillSpeedFactor();
	
	/**
	* @return the infill feedrate as a value in mm/minute
	*/
	public double getInfillFeedrate();

	/**
	 * @return the outline speed as a avlue between [0,1]
	 */
	public double getOutlineSpeedFactor();

	/**
	* @return the infill feedrate as a value in mm/minute
	*/
	public double getOutlineFeedrate();
	
	/**
	 * @return The length in mm to speed up when going round corners
	 */
	public double getAngleSpeedUpLength();

	/**
	 * The factor by which to speed up when going round a corner.
	 * The formula is speed = baseSpeed*[1 - 0.5*(1 + ca)*getAngleSpeedFactor()]
	 * where ca is the cos of the angle between the lines.  So it goes fastest when
	 * the line doubles back on itself (returning 1), and slowest when it 
	 * continues straight (returning 1 - getAngleSpeedFactor()).
	 * @return the angle-speed factor 
	 */
	public double getAngleSpeedFactor();
	
	/**
	* @return the angle feedrate as a value in mm/minute
	*/
	public double getAngleFeedrate();
	
	/**
	 * Turn the cooler (fan?) on or off
	 * @param f true if the cooler is to be turned on, false to turn off
	 * @throws IOException
	 * @throws Exception 
	 */
	public void setCooler(boolean f) throws IOException, Exception ;
	
	/**
	 * Check if the extruder is available, which is determined by ???
	 * @return true if the extruder is available
	 */
	public boolean isAvailable(); 

    /**
     * The speed of X and Y movement
     * @return the XY feedrate in mm/minute
     */
    public double getFastXYFeedrate();
    
    /**
     * The fastest we can extrude in mm/min
     * @return
     */
    public double getFastEFeedrate();
    
	/**
	 * @return slow XY movement feedrate in mm/minute
	 */
	public double getSlowXYFeedrate();
	
	/**
	 * @return the fastest the machine can accelerate
	 */
	public double getMaxAcceleration();
 
    /**
     * @return the extruder speeds
     */
    public double getExtruderSpeed(); 
    
    /**
     * Time to purge the extruder
     * -ve values supress
     * @return
     */
    public double getPurgeTime();
    
    /**
     * Purge the extruder
     *
     */
    public void purge(boolean homeZ) throws Exception;
    
    /**
     * Set the flag to show we're creating a separation
     * @param s
     */
    public void setSeparating(boolean s);

    /**
     * @return the extrusion size in millimeters
     */
    public double getExtrusionSize();
 
    /**
     * @return the extrusion height in millimeters
     */
    public double getExtrusionHeight();

    /**
     * @return the cooling period in seconds
     */
    public double getCoolingPeriod();
    
    /**
     * Find out what our current speed is
     * @return
     */
    public double getCurrentSpeed();
    
    /**
     * Find out if we are currently in reverse
     * @return
     */
    public boolean getReversing();
    
    /**
     * Find out if we're working in 5D
     * @return
     */
    public boolean get5D();
 
    /**
     * Get how much extrudate is deposited in a given time
     * @param time
     * @return
     */
    public double getDistanceFromTime(double time);
    
    /**
     * Get how much extrudate is deposited for a given movement
     * @param distance
     * @return
     */
    public double getDistance(double distance);
    
    /**
     * Find out how far we have extruded so far
     * @return
     */
    public ExtruderState getExtruderState() throws Exception;
    
	/**
	 * Allow otthers to set our extrude length so that all logical extruders
	 * talking to one physical extruder can use the same length instance.
	 * @param e
	 */
	public void setExtrudeState(ExtruderState e);
	
	/**
	 * Zero the extruded length
	 * @throws Exception 
	 *
	 */
	public void zeroExtrudedLength() throws Exception;
    
    /**
     * @return the X offset in millimeters
     */
    public double getOffsetX();
 
    /**
     * @return the Y offset in millimeters
     */
    public double getOffsetY();
 
    /**
     * @return the Z offset in millimeters
     */
    public double getOffsetZ();
    
    /**
     * @return the appearance (colour) to use in the simulation window for this material
     */
    public Appearance getAppearance();  

    /**
     * Each logical extruder has a unique ID
     * @return
     */
    public int getID();
    
    /**
     * Several logical extruders can share one physical extruder
     * This number is unique to each physical extruder
     * @return
     */
    public int getPhysicalExtruderNumber();
    
    /**
     * @return whether nozzle wipe method is enabled or not 
     */
    public boolean getNozzleWipeEnabled();
    
    /**
     * @return the X-cord for the nozzle wiper
     */
    public double getNozzleWipeDatumX();

    /**
     * @return the Y-cord for the nozzle wiper
     */
    public double getNozzleWipeDatumY();
    
    /**
     * @return the X length of the nozzle movement over the wiper
     */
    public double getNozzleWipeStrokeX();
    
    /**
     * @return the Y length of the nozzle movement over the wiper
     */
    public double getNozzleWipeStrokeY();
    
    /**
     * @return the number of times the nozzle moves over the wiper
     */
    public int getNozzleWipeFreq();
    
    /**
     * @return the time to extrude before wiping the nozzle
     */
    public double getNozzleClearTime();
    
    /**
     * @return the time to wait after wiping the nozzle
     */
    public double getNozzleWaitTime();
    
    /**
     * Start polygons at a random location round their perimiter
     * @return
     */
    public boolean randomStart();

    /**
     * Start polygons at an incremented location round their perimiter
     * @return
     */
    public boolean incrementedStart();
    
    /**
     * If this is true, plot outlines from the middle of their infilling hatch to reduce dribble at
     * their starts and ends.  If false, plot the outline as the outline.
     * @return
     */
    public boolean getMiddleStart();
    
    /**
     * get short lengths which need to be plotted faster
     * set -ve to turn this off.
     * @return
     */
    public double getShortLength();
    
    /**
     * Factor (between 0 and 1) to use to set the speed for
     * short lines.
     * @return
     */
    public double getShortLineSpeedFactor();

	/**
	* Feedrate for short lines in mm/minute
	* @return
	*/
	public double getShortLineFeedrate();
    
    /**
     * Number of mm to overlap the hatching infill with the outline.  0 gives none; -ve will 
     * leave a gap between the two
     * @return
     */
    public double getInfillOverlap();
    
    /**
	 * Gets the number of milliseconds to wait before starting the extrude motor
	 * for the first track of a layer
	 * @return
     */
    public double getExtrusionDelayForLayer();
    
    /**
	 * Gets the number of milliseconds to wait before starting the extrude motor
	 * for any other track
	 * @return
     */
    public double getExtrusionDelayForPolygon();
    
    /**
	 * Gets the number of milliseconds to reverse the extrude motor
	 * at the end of a track
	 * @return
     */
    public double getExtrusionReverseDelay();
    
    /**
	 * Gets the number of milliseconds to wait before opening the valve
	 * for the first track of a layer
	 * @return
     */
    public double getValveDelayForLayer();
    
    /**
	 * Gets the number of milliseconds to wait before opening the valve
	 * for any other track
	 * @return
     */
    public double getValveDelayForPolygon();
    
    /**
     * @return the extrusion overrun in millimeters (i.e. how many mm
     * before the end of a track to turn off the extrude motor)
     */
    public double getExtrusionOverRun();
    
    /**
     * @return the valve overrun in millimeters (i.e. how many mm
     * before the end of a track to turn off the extrude motor)
     */
    public double getValveOverRun();
    
    /**
     * The number of times to go round the outline (0 to supress)
     * @return
     */
    public int getShells();
    
    /**
     * The smallest allowable free-movement height above the base
     * @return
     */
    public double getMinLiftedZ();
    
    /**
     * Stop the extrude motor between segments?
     * @return
     */
    public boolean getPauseBetweenSegments();
    
    /**
     * 
     * @param p
     */
	public void setPrinter(Printer p);

	/**
	 * 
	 * @return
	 */
	public Printer getPrinter();
	
	/**
	 * 
	 * @return
	 */
	public double getExtrusionFoundationWidth();
	
	/**
	 * 
	 * @return
	 */
	public double getExtrusionInfillWidth();
	
	/**
	 * How high to lift above the surface for in-air movements
	 * @return
	 */
	public double getLift();
	
	/**
	 * 
	 * @return
	 */
	public double getExtrusionBroadWidth();
	
	
	public int getLowerFineLayers();
	public int getUpperFineLayers();
	
	/**
	 * At the support layer before a layer is to be separated, how far up
	 * the normal Z movement do we go to make a bigger gap to form a weak join?
	 * @return
	 */
	public double getSeparationFraction();
	
	/**
	 * Wait if the XY movement buffer is active in the mictrocontroller
	 * @throws IOException
	 */
	public void waitTillNotBusy() throws IOException;
    
	/**
	 * The arc compensation factor.  
	 * See org.reprap.geometry.polygons.RrPolygon.arcCompensate(...)
	 * @return
	 */
	public double getArcCompensationFactor();
	
	/**
	 * The arc short sides.  
	 * See org.reprap.geometry.polygons.RrPolygon.arcCompensate(...)
	 * @return
	 */
	public double getArcShortSides();
	
	/**
	 * What stuff are we working with?
	 * @return
	 */
	public String getMaterial();
	
	/**
	 * What stuff are we supporting with?
	 * @return
	 */
	public String getSupportMaterial();
	
	public int getSupportExtruderNumber();
	
	public Extruder getSupportExtruder();
	
	/**
	 * What stuff are we infilling with?
	 * @return
	 */
	public String getInfillMaterial();
	
	public int getInfillExtruderNumber();
	
	public Extruder getInfillExtruder();
	
	/**
	 * What are the dimensions for infill?
	 * @return
	 */
	//public String getBroadInfillMaterial();
	
	/**
	 * The direction to hatch even-numbered layers in degrees anticlockwise
	 * from the X axis
	 * @return
	 */
	public double getEvenHatchDirection();

	/**
	 * The direction to hatch odd-numbered layers in degrees anticlockwise
	 * from the X axis
	 * @return
	 */
	public double getOddHatchDirection();
	   /**
     * Get the extrude ratio
     * @return
     */
    public double getExtrudeRatio();

    
    /**
     * Set the extrude ratio.  Only to be used if you know what you
     * are doing.  It's a good idea to set it back when you've finished...
     * @param er
     */
    public void setExtrudeRatio(double er);
	
	
}
