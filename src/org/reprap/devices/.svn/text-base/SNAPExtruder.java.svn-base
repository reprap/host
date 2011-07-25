//package org.reprap.devices;
//
//import java.io.IOException;
//
//import org.reprap.Device;
//import org.reprap.Printer;
//import org.reprap.Preferences;
//import org.reprap.utilities.Debug;
//import org.reprap.comms.Address;
//import org.reprap.comms.Communicator;
//import org.reprap.comms.IncomingMessage;
//import org.reprap.comms.OutgoingMessage;
//import org.reprap.comms.IncomingMessage.InvalidPayloadException;
//import org.reprap.comms.messages.OutgoingBlankMessage;
//import org.reprap.comms.messages.OutgoingByteMessage;
//import org.reprap.comms.IncomingContext;
//import org.reprap.comms.messages.VersionRequestMessage;
//import org.reprap.comms.messages.VersionResponseMessage;
//
///**
// * @author jwiel
// *
// */
//public class SNAPExtruder extends GenericExtruder
//{
//	/**
//	 * Communicator
//	 * 
//	 */
//	private Communicator communicator = null;
//	
//	/**
//	 * API for firmware
//	 * Activate the extruder motor in forward direction 
//	 */
//	public static final byte MSG_SetActive = 1;
//	
//	/**
//	 *  Activate the extruder motor in reverse direction
//	 */
//	public static final byte MSG_SetActiveReverse = 2;
//	
//	/**
//	 * There is no material left to extrude 
//	 */
//	public static final byte MSG_IsEmpty = 8;
//	
//	/**
//	 * Set the temperature of the extruder
//	 */
//	public static final byte MSG_SetHeat = 9;
//	
//	/**
//	 * Get the temperature of the extruder 
//	 */
//	public static final byte MSG_GetTemp = 10;
//		
//	/**
//	 * Turn the cooler/fan on 
//	 */
//	public static final byte MSG_SetCooler = 11;
//	
//	
//	/**
//	 * Open the valve 
//	 */
//	public static final byte MSG_ValveOpen = 12;
//	
//	/**
//	 *  Close the valve
//	 */
//	public static final byte MSG_ValveClosed = 13;	
//	
//	/**
//	 * Set Vref 
//	 */
//	public static final byte MSG_SetVRef = 52;
//	
//	/**
//	 * Set the Tempscaler 
//	 */
//	public static final byte MSG_SetTempScaler = 53;
//	 
//	/**
//	 * Temprature history
//	 */
//	@SuppressWarnings("unused")
//	private double[] tH;
//	@SuppressWarnings("unused")
//	private int tHi;
//	
//	/**
//	 * Is a material-out sensor connected to the exteruder or not. 
//	 * If this is the case, TODO: impact?
//	 */
//	private boolean currentMaterialOutSensor = false;
//	
//	/**
//	 * 
//	 */
//	private Thread pollThread = null;
//	
//	/**
//	 * 
//	 */
//	@SuppressWarnings("unused")
//	private boolean pollThreadExiting = false;
//	
//	/**
//	 * 
//	 */
//	private int vRefFactor = 7;
//	
//	/**
//	 * 
//	 */
//	private int tempScaler = 4;
//	
//	
//	/**
//	 * Thermistor beta
//	 */
//	private double beta; 
//	
//	/**
//	 * Thermistor resistance at 0C
//	 */
//	private double rz;  
//	
//	/**
//	 * Thermistor timing capacitor in farads
//	 */
//	private double cap;    
//
//	/**
//	 * Heater power gradient
//	 */
//	private double hm;   
//
//	/**
//	 * Heater power intercept
//	 * TODO: hb should probably be ambient temperature measured at this point
//	 */
//	private double hb;
//	
//	/**
//	* our snap comms device.
//	*/
//	private Device snap; 
//	
//	private boolean wasAlive = false;
//	
//	/**
//	 * What firmware are we running?
//	 */
//	protected int firmwareVersion = 0;
//	
//	/**
//	 * 
//	 */
//	private long lastTemperatureUpdate = 0;
//	
//	public SNAPExtruder(Communicator com, Address address, int extruderId, Printer p)
//	{
//		super(extruderId, p);
//		communicator = com;
//		snap = new Device(communicator, address);
//		isAvailable();
//		
//		tH = new double[] {20, 20, 20}; // Bit of a hack - room temp
//		tHi = 0;
//		
//		//Anyone home?
//		if(!isAvailable())
//			return;
//		
//		try
//		{
//			firmwareVersion = getVersion();
//		} catch (Exception ex) {}
//		
//		// Set up thermometer
//		try {
//			setTempRange();
//		} catch (Exception ex) {
//			return;
//		}
//		
//
//		/*pollThread = new Thread() {
//			public void run() {
//				Thread.currentThread().setName("Extruder poll");
//				boolean first = true;
//				while(!pollThreadExiting) {
//					try {
//						// Sleep is beforehand to prevent runaway on exception
//						if (!first) Thread.sleep(2000);
//						first = false;
//						RefreshTemperature();
//						RefreshEmptySensor();
//						sensorsInitialised = true;
//					}
//					catch (InterruptedException ex) {
//						// This is normal when shutting down, so ignore
//					}
//					catch (Exception ex) {
//						System.out.println("Exception during temperature poll");
//						ex.printStackTrace();
//					}
//				}
//			}
//		};
//		pollThread.start();*/
//	}
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
//	 * Purge the extruder
//	 */
//	public void purge(boolean homeZ)
//	{
//		if(purgeTime <= 0)
//			return;
//		getPrinter().moveToPurge();
//		try
//		{
//			if(homeZ)
//				getPrinter().homeToZeroZ();
//			setExtrusion(getFastXYFeedrate(), false);
//			getPrinter().machineWait(purgeTime, false);
//			setExtrusion(0, false);
//		} catch (Exception e)
//		{}
//		try {
//			zeroExtrudedLength();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
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
//		return communicator.sendMessage(snap, message);
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
//	
//	public int refreshPreferences()
//	{
//		int result = super.refreshPreferences();
//		
//		try
//		{
//			beta = Preferences.loadGlobalDouble(prefName + "Beta(K)");
//			rz = Preferences.loadGlobalDouble(prefName + "Rz(ohms)");
//			cap = Preferences.loadGlobalDouble(prefName + "Capacitor(F)");
//			hm = Preferences.loadGlobalDouble(prefName + "hm(C/pwr)");
//			hb = Preferences.loadGlobalDouble(prefName + "hb(C)");
//		} catch (Exception ex)
//		{
//			System.err.println("Refresh extruder preferences: " + ex.toString());
//		}
//		
//		return result;
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Extruder#dispose()
//	 */
//	public void dispose()
//	{
//		if (pollThread != null) {
//			pollThreadExiting = true;
//			pollThread.interrupt();
//		}
//	}
//
//	/**
//	 * Start the extruder motor at a given speed.  This ranges from 0
//	 * to 255 but is scaled by maxSpeed and t0, so that 255 corresponds to the
//	 * highest permitted speed.  It is also scaled so that 0 would correspond
//	 * with the lowest extrusion speed.
//	 * @param speed The speed to drive the motor at (0-255)
//	 * @param reverse If set, run extruder in reverse
//	 * @throws IOException
//	 */
//	public void setExtrusion(double speed, boolean reverse) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return;
//		}
//		
//		Debug.d("Extruding at speed: " + speed);
//		
//		// Assumption: Between t0 and maxSpeed, the speed is fairly linear
//		int scaledSpeed;
//		
//		if (speed > 0)
//			scaledSpeed = (int)Math.round((maxExtruderSpeed - t0) * speed / 255.0 + t0);
//		else
//			scaledSpeed = 0;
//		waitTillNotBusy();
//		lock();
//		try {
//			OutgoingMessage request =
//				new OutgoingByteMessage(reverse ? MSG_SetActiveReverse : MSG_SetActive,
//						(byte)scaledSpeed);
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * Open or close the valve.  pulseTime is the number of ms to zap it.
//	 * @param pulseTime
//	 * @param valveOpen
//	 * @throws IOException
//	 */
//	public void setValve(boolean valveOpen) throws IOException {
//		
//		if(valvePulseTime < 0)
//			return;		
//		
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return;
//		}
//		
//		if(valveOpen)
//			Debug.d("Opening valve.");
//		else
//			Debug.d("Closing valve.");
//		
//		waitTillNotBusy();
//		communicator.lock();
//		try {
//			OutgoingMessage request =
//				new OutgoingByteMessage(valveOpen ? MSG_ValveOpen : MSG_ValveClosed,
//						(byte)valvePulseTime);
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Extruder#setTemperature(double)
//	 */
//	public void setTemperature(double temperature, boolean wait) throws Exception {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return;
//		}
//		setTemperatureX(temperature, true);
//	}
//	
//	/**
//	 * @param temperature
//	 * @param lock
//	 * @throws Exception
//	 */
//	private void setTemperatureX(double temperature, boolean lock) throws Exception {
//		es.setTargetTemperature(temperature);
//		if(Math.abs(es.targetTemperature() - extrusionTemp) > 5)
//		{
//			Debug.d(material + " extruder temperature set to " + es.targetTemperature() +
//				"C, which is not the standard temperature (" + extrusionTemp + "C).");
//		}
//		// Aim for 10% above our target to ensure we reach it.  It doesn't matter
//		// if we go over because the power will be adjusted when we get there.  At
//		// the same time, if we aim too high, we'll overshoot a bit before we
//		// can react.
//		
//		// Tighter temp constraints under test 10% -> 3% (10-1-8)
//		double temperature0 = temperature * 1.03;
//		
//		// A safety cutoff will be set at 20% above requested setting
//		// Tighter temp constraints added by eD 20% -> 6% (10-1-8)
//		
//		double temperatureSafety = temperature * 1.06;
//		
//		// Calculate power output from hm, hb.  In general, the temperature
//		// we achieve is power * hm + hb.  So to achieve a given temperature
//		// we need a power of (temperature - hb) / hm
//		
//		// If we reach our temperature, rather than switching completely off
//		// go to a reduced power level.
//		int power0 = (int)Math.round(((0.9 * temperature0) - hb) / hm);
//		if (power0 < 0) power0 = 0;
//		if (power0 > 255) power0 = 255;
//
//		// Otherwise, this is the normal power level we will maintain
//		int power1 = (int)Math.round((temperature0 - hb) / hm);
//		if (power1 < 0) power1 = 0;
//		if (power1 > 255) power1 = 255;
//
//		// Now convert temperatures to equivalent raw PIC temperature resistance value
//		// Here we use the original specified temperature, not the slight overshoot
//		double resistance0 = calculateResistanceForTemperature(temperature);
//		double resistanceSafety = calculateResistanceForTemperature(temperatureSafety);
//
//		// Determine equivalent raw value
//		int t0 = calculatePicTempForResistance(resistance0);
//		if (t0 < 0) t0 = 0;
//		if (t0 > 255) t0 = 255;
//		int t1 = calculatePicTempForResistance(resistanceSafety);
//		if (t1 < 0) t1 = 0;
//		if (t1 > 255) t1 = 255;
//		
//		if (temperature == 0)
//			setHeater(0, 0, lock);
//		else {
//			setHeater(power0, power1, t0, t1, lock);
//		}
//	}
//	
//	/**
//	 * Set a heat output power.  For normal production use you would
//	 * normally call setTemperature, however this method may be useful
//	 * for lower temperature profiling, etc.
//	 * @param heat Heater power (0-255)
//	 * @param maxTemp Cutoff temperature in celcius
//	 * @throws IOException
//	 */
//	public void setHeater(int heat, double maxTemp) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return;
//		}
//
//		double safetyResistance = calculateResistanceForTemperature(maxTemp);
//		// Determine equivalent raw value
//		int safetyPICTemp = calculatePicTempForResistance(safetyResistance);
//		if (safetyPICTemp < 0) safetyPICTemp = 0;
//		if (safetyPICTemp > 255) safetyPICTemp = 255;
//
//		if (heat == 0)
//			setHeater(0, 0, true);
//		else
//			setHeater(heat, safetyPICTemp, true);
//
//	}
//	
//	/**
//	 * Set the raw heater output value and safety cutoff.  A specific
//	 * temperature can be reached by setting a suitable output power.
//	 * A limit temperature can also be specified.  If reached, the
//	 * heater will be automatically turned off until the temperature
//	 * drops below the limit.
//	 * @param heat A heater power output
//	 * @param safetyCutoff A temperature at which to cut off the heater
//	 * @throws IOException
//	 */
//	private void setHeater(int heat, int safetyCutoff, boolean lock) throws IOException {
//		//System.out.println(material + " extruder heater set to " + heat + " limit " + safetyCutoff);
//		
//		if (lock) 
//		{
//			waitTillNotBusy();
//			communicator.lock();
//		}
//		try {
//			communicator.sendMessage(snap, new RequestSetHeat((byte)heat, (byte)safetyCutoff));
//		}
//		finally {
//			if (lock) communicator.unlock();
//		}
//	}
//
//	/**
//	 * Similar to setHeater(int, int) except this provides two different
//	 * heating zones.  Below t0, it heats at h1.  From t0 to t1 it heats at h0
//	 * and above t1 it shuts off.  This has the effect of still providing some
//	 * power when the desired temperature is reached so it cools less quickly.   
//	 * @param heat0
//	 * @param heat1
//	 * @param t0
//	 * @param t1
//	 * @throws IOException
//	 */
//	private void setHeater(int heat0, int heat1, int t0, int t1, boolean lock) throws IOException {
//		Debug.d(material + " extruder heater set to " + heat0 + "/" + heat1 + " limit " + t0 + "/" + t1);
//		
//		if (lock)
//		{
//			waitTillNotBusy();
//			communicator.lock();
//		}
//		try {
//			communicator.sendMessage(snap, new RequestSetHeat((byte)heat0,
//										   (byte)heat1,
//										   (byte)t0,
//										   (byte)t1));
//		}
//		finally {
//			if (lock) communicator.unlock();
//		}
//	}
//
//	/**
//	 * Check if the extruder is out of feedstock
//	 * @return true if there is no material remaining
//	 */
//	public boolean isEmpty() {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return true;
//		}
//		awaitSensorsInitialised();
//		TEMPpollcheck();
//		return currentMaterialOutSensor;
//	}
//	
//	/**
//	 * 
//	 */
//	private void awaitSensorsInitialised() {
//		// Simple minded wait to let sensors become valid
//		//while(!sensorsInitialised) {
//		//	try {
//		//		Thread.sleep(100);
//		//	} catch (InterruptedException e) {
//		//	}
//		//}
//	}
//	
//	/**
//	 * Send current vRefFactor and a suitable timer scaler
//	 * to the device. 
//	 *
//	 */
//	private void setTempRange() throws Exception
//	{
//		// We will send the vRefFactor to the PIC.  At the same
//		// time we will send a suitable temperature scale as well.
//		// To maximize the range, when vRefFactor is high (15) then
//		// the scale is minimum (0).
//		Debug.d(material + " extruder vRefFactor set to " + vRefFactor);
//		tempScaler = 7 - (vRefFactor >> 1);
//	    setVref(vRefFactor);
//		setTempScaler(tempScaler);
//		if (es.targetTemperature() != 0)
//			setTemperatureX(es.targetTemperature(), false);
//	}
//	
//	/**
//	 * Called internally to refresh the empty sensor.  This is
//	 * called periodically in the background by another thread.
//	 * @throws IOException
//	 */
//	private void RefreshEmptySensor() throws IOException {
//		// TODO in future, this should use the notification mechanism rather than polling (when fully working)
//		waitTillNotBusy();
//		communicator.lock();
//		try {
//			//System.out.println(material + " extruder refreshing sensor");
//			RequestIsEmptyResponse reply = new RequestIsEmptyResponse(snap, new OutgoingBlankMessage(MSG_IsEmpty), 500);
//			currentMaterialOutSensor = reply.getValue() == 0 ? false : true; 
//		} catch (InvalidPayloadException e) {
//			throw new IOException();
//		} finally {
//			communicator.unlock();
//		}
//	}
//
//	/**
//	 * TEMPORARY WORKAROUND FUNCTION
//	 */
//	private void TEMPpollcheck() {
//		if (System.currentTimeMillis() - lastTemperatureUpdate > 10000) {
//			// Polled updates are having a hard time getting through with
//			// the temporary comms locking, so we'll get them through here
//			try {
//				RefreshEmptySensor();
//				RefreshTemperature();
//				//tH[tHi] = currentTemperature;
//				//currentTemperature = tempVote();
//			} catch (Exception ex) {
//				Debug.d(material + " extruder exception during temperature/material update ignored");
//				ex.printStackTrace();
//			}
//		}
//	}
//	
//	//	/**
//	//	 * Take a vote among the last three temperatures
//	//	 * @return
//	//	 */
//	//	private double tempVote()
//	//	{
//	//		int ip = (tHi + 1)%3;
//	//		int ipp = (tHi + 2)%3;
//	//		double dp = Math.abs(tH[tHi] - tH[ip]);
//	//		double dpp = Math.abs(tH[tHi] - tH[ipp]);
//	//		double d = Math.abs(tH[ip] - tH[ipp]);
//	//		if(dp <= d || dpp <= d)
//	//			d = tH[tHi];
//	//		else
//	//			d = 0.5*(tH[ip] + tH[ipp]);
//	//		//Debug.d("tempVote() - t0: " + tH[0] + ", t1: " + tH[1] + ", t2: " + tH[2] +
//	//				//", current: " + tH[tHi] + ", returning: " + d);
//	//		tHi = (tHi + 1)%3;
//	//		return d;
//	//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Extruder#getTemperature()
//	 */
//	public double getTemperature() {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return Preferences.absoluteZero();
//		}
//		awaitSensorsInitialised();
//		TEMPpollcheck();
//		//return tempVote();
//		return es.currentTemperature();
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.reprap.Extruder#setCooler(boolean)
//	 */
//	public void setCooler(boolean f) throws IOException {
//		if(!wasAvailable())
//		{
//			Debug.d("Attempting to control or interrogate non-existent extruder for " + material);
//			return;
//		}
//		//System.out.println("setCooler 1");
//		waitTillNotBusy();
//		//System.out.println("setCooler 2");
//		communicator.lock();
//		try {
//			OutgoingMessage request =
//				new OutgoingByteMessage(MSG_SetCooler, f?(byte)200:(byte)0); // Should set speed properly!
//			communicator.sendMessage(snap, request);
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//	
//	/**
//	 * @throws Exception
//	 */
//	private void RefreshTemperature() throws Exception {
//		//System.out.println(material + " extruder refreshing temperature");
//		getDeviceTemperature();
//	}
//	
//	/**
//	 * 
//	 * @param rawHeat
//	 * @return
//	 * @throws Exception
//	 */
//	private boolean rerangeTemperature(int rawHeat) throws Exception 
//	{
//		boolean notDone = false;
//		if (rawHeat == 255 && vRefFactor > 0) {
//			vRefFactor--;
//			Debug.d(material + " extruder re-ranging temperature (faster): ");
//			setTempRange();
//		} else if (rawHeat < 64 && vRefFactor < 15) {
//			vRefFactor++;
//			Debug.d(material + " extruder re-ranging temperature (slower): ");
//			setTempRange();
//		} else
//			notDone = true;
//		return notDone;
//	}
//	
//	/**
//	 * 
//	 * @throws Exception
//	 */
//	private void getDeviceTemperature() throws Exception {
//		waitTillNotBusy();
//		communicator.lock();
//		try {
//			int rawHeat = 0;
//			int calibration = 0;
//			for(;;) { // Don't repeatedly re-range?
//				OutgoingMessage request = new OutgoingBlankMessage(MSG_GetTemp);
//				RequestTemperatureResponse reply = new RequestTemperatureResponse(snap, request, 500);
//				
//				rawHeat = reply.getHeat();
//				//System.out.println(material + " extruder raw temp " + rawHeat);
//				
//				// Note that for the Arduino reply.getCalibration(); returns the
//				// actual temperature in honest-to-god deg C.  We should probably
//				// use that...
//				calibration = reply.getCalibration();
//				//System.out.println(material + " temp byte 2: " + calibration);
//				if(rerangeTemperature(rawHeat))
//					break; // All ok
//				else
//					printer.machineWait(500, false);
//					//Thread.sleep(500); // Wait for PIC temp routine to settle before going again
//			}
//			
//			double resistance = calculateResistance(rawHeat, calibration);
//			
//			es.setCurrentTemperature(calculateTemperature(resistance));
//			Debug.d(material + " extruder current temp " + es.currentTemperature());
//			
//			lastTemperatureUpdate = System.currentTimeMillis();
//		}
//		finally {
//			communicator.unlock();
//		}
//	}
//
//	/**
//	 * Calculates the actual resistance of the thermistor
//	 * from the raw timer values received from the PIC. 
//	 * @param picTemp
//	 * @param calibrationPicTemp
//	 * @return
//	 */
//	private double calculateResistance(int picTemp, int calibrationPicTemp) {
//		// TODO remove hard coded constants
//		// TODO should use calibration value instead of first principles
//		
//		//double resistor = 10000;                   // ohms
//		//double c = 1e-6;                           // farads - now cap from prefs(AB)
//		double scale = 1 << (tempScaler+1);
//		double clock = 4000000.0 / (4.0 * scale);  // hertz		
//		double vdd = 5.0;                          // volts
//		
//		double vRef = 0.25 * vdd + vdd * vRefFactor / 32.0;  // volts
//		
//		double T = picTemp / clock; // seconds
//		
//		double resistance =	-T / (Math.log(1 - vRef / vdd) * cap);  // ohms
//		
//		return resistance;
//	}
//
//	/**
//	 * Calculate temperature in celsius given resistance in ohms
//	 * @param resistance
//	 * @return
//	 */
//	private double calculateTemperature(double resistance) {
//		
//		return (1.0 / (1.0 / absZero + Math.log(resistance/rz) / beta)) - absZero;
//	}
//	
//	/**
//	 * @param temperature
//	 * @return
//	 */
//	private double calculateResistanceForTemperature(double temperature) {
//		return rz * Math.exp(beta * (1/(temperature + absZero) - 1/absZero));
//	}
//	
//	/**
//	 * Calculates an expected PIC Temperature expected for a
//	 * given resistance 
//	 * @param resistance
//	 * @return
//	 */
//	private int calculatePicTempForResistance(double resistance) {
//		//double c = 1e-6;                           // farads - now cap from prefs(AB)
//		double scale = 1 << (tempScaler+1);
//		double clock = 4000000.0 / (4.0 * scale);  // hertz		
//		double vdd = 5.0;                          // volts
//		
//		double vRef = 0.25 * vdd + vdd * vRefFactor / 32.0;  // volts
//		
//		double T = -resistance * (Math.log(1 - vRef / vdd) * cap);
//
//		double picTemp = T * clock;
//		return (int)Math.round(picTemp);
//		
//	}
//	
//	/**
//	 * Set raw voltage reference used for analogue to digital converter
//	 * @param ref Set reference voltage (0-15).  Actually this is
//	 * just directly OR'd into the PIC VRCON register, so it can also
//	 * set the High/Low range bit.  
//	 * @throws IOException
//	 */
//	private void setVref(int ref) throws IOException {
//		communicator.sendMessage(snap, new OutgoingByteMessage(MSG_SetVRef, (byte)ref));		
//		vRefFactor = ref;
//	}
//
//	/**
//	 * Set the scale factor used on the temperature timer used
//	 * for analogue to digital conversion
//	 * @param scale A value from 0..7
//	 * @throws IOException
//	 */
//	private void setTempScaler(int scale) throws IOException {
//		communicator.sendMessage(snap, new OutgoingByteMessage(MSG_SetTempScaler, (byte)scale));		
//		tempScaler = scale;
//	}
//	
//	/**
//	 *
//	 */
//	protected class RequestTemperatureResponse extends IncomingMessage {
//		
//		/**
//		 * @param device
//		 * @param message
//		 * @param timeout
//		 * @throws IOException
//		 */
//		public RequestTemperatureResponse(Device device, OutgoingMessage message, 
//				long timeout) throws IOException {
//			super(device, message, timeout);
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.IncomingMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_GetTemp; 
//		}
//		
//		/**
//		 * @return
//		 * @throws InvalidPayloadException
//		 */
//		public int getHeat() throws InvalidPayloadException {
//		    byte [] reply = getPayload();
//		    if (reply == null || reply.length != 3)
//		    		throw new InvalidPayloadException();
//		    return reply[1] < 0 ? reply[1] + 256 : reply[1];
//		}
//
//		/**
//		 * @return
//		 * @throws InvalidPayloadException
//		 */
//		public int getCalibration() throws InvalidPayloadException {
//		    byte [] reply = getPayload();
//		    if (reply == null || reply.length != 3)
//		    		throw new InvalidPayloadException();
//		    return reply[2] < 0 ? reply[2] + 256 : reply[2];
//		}
//		
//	}
//
//	/**
//	 *
//	 */
//	protected class RequestSetHeat extends OutgoingMessage {
//		
//		/**
//		 * 
//		 */
//		byte [] message;
//		
//		/**
//		 * @param heat
//		 * @param cutoff
//		 */
//		RequestSetHeat(byte heat, byte cutoff) {
//			message = new byte [] { MSG_SetHeat, heat, heat, cutoff, cutoff }; 
//		}
//
//		/**
//		 * @param heat0
//		 * @param heat1
//		 * @param t0
//		 * @param t1
//		 */
//		RequestSetHeat(byte heat0, byte heat1, byte t0, byte t1) {
//			message = new byte [] { MSG_SetHeat, heat0, heat1, t0, t1}; 
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
//	protected class RequestIsEmptyResponse extends IncomingMessage {
//
//		/**
//		 * @param device
//		 * @param message
//		 * @param timeout
//		 * @throws IOException
//		 */
//		public RequestIsEmptyResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
//			super(device, message, timeout);
//		}
//		
//		/**
//		 * @return
//		 * @throws InvalidPayloadException
//		 */
//		public byte getValue() throws InvalidPayloadException {
//			byte [] reply = getPayload();
//			if (reply == null || reply.length != 2)
//				throw new InvalidPayloadException();
//			return reply[1];
//		}
//		
//		/* (non-Javadoc)
//		 * @see org.reprap.comms.IncomingMessage#isExpectedPacketType(byte)
//		 */
//		protected boolean isExpectedPacketType(byte packetType) {
//			return packetType == MSG_IsEmpty;
//		}
//		
//	}
//	
//}
