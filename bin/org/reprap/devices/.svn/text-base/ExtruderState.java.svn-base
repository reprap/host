package org.reprap.devices;

/**
 * Tiny class to hold the length of filament that an extruder has extruded so far, and
 * other aspects of the state of the extruder.  This is used in the extruder class.
 * All logical extruders that correspond to one physical extruder share a single instance of
 * this class.  This means that (for example) setting the temperature of one will be reflected
 * automatically in all the others.
 * 
 * @author Adrian
 *
 */
public class ExtruderState 
{
	private double l;  // Extruded length
	private double tt; // Set temperature
	private double ct; // Current temperature	
	private double s;  // Motor speed
	private boolean r; // Are we going backwards?
	private boolean e; // Are we extrudeing
	private int pe;    // The physical extruder
	
	ExtruderState(int physEx)
	{
		l = 1;
		tt = 0;
		ct = 0;
		s = 0;
		r = false;
		e = false;
		pe = physEx;
	}
	
	public int physicalExtruder()
	{
		return pe;
	}
	
	public double length()
	{
		return l;
	}
	
	public double targetTemperature()
	{
		return tt;
	}
	
	public double currentTemperature()
	{
		return ct;
	}
	
	public double speed()
	{
		return s;
	}
	
	public boolean reverse()
	{
		return r;
	}
	
	public boolean isExtruding()
	{
		return e;
	}
	
	public void add(double e)
	{
		l += e;
	}
	
	public void zero()
	{
		l = 0;
	}
	
	public void setTargetTemperature(double temp)
	{
		tt = temp;
	}
	
	public void setCurrentTemperature(double temp)
	{
		ct = temp;
	}
	
	public void setSpeed(double sp)
	{
		s = sp;
	}
	
	public void setReverse(boolean rev)
	{
		r = rev;
	}
	
	public void setExtruding(boolean ex)
	{
		e = ex;
	}
}
